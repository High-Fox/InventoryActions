package highfox.inventoryactions.util;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.ActionContext;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class UtilCodecs {
	public static final Gson GSON = Deserializers.createFunctionSerializer().create();

	public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(str -> {
		try {
			return DataResult.success(Pattern.compile(str));
		} catch (PatternSyntaxException e) {
			return DataResult.error(e.getMessage());
		}
	}, Pattern::pattern);
	public static final Codec<SourceOrItem> SOURCE_OR_ITEM_CODEC = Codec.either(ItemSource.CODEC, RegistryFileCodec.create(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS.getCodec())).xmap(SourceOrItem::new, SourceOrItem::either);
	public static final Codec<NumberProvider> NUMBER_PROVIDER_CODEC = jsonSerializerCodec(NumberProvider.class, "number provider");
	public static final Codec<LootItemFunction[]> ITEM_FUNCTIONS_CODEC = jsonSerializerCodec(LootItemFunction[].class, "item functions");

	public static <T> Codec<List<T>> singleOrList(Codec<T> codec) {
		Codec<Either<T, List<T>>> eitherCodec = Codec.either(codec, codec.listOf());
		return eitherCodec.xmap(either -> {
			return either.map(ImmutableList::of, Function.identity());
		}, list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list));
	}

	public static <P, I> Codec<I> rangedCodec(Codec<P> typeCodec, String leftFieldName, String rightFieldName, BiFunction<Optional<P>, Optional<P>, DataResult<I>> combineFunction, Function<I, P> leftGetter, Function<I, P> rightGetter) {
		Codec<I> pairCodec = RecordCodecBuilder.<Pair<Optional<P>,Optional<P>>>create(instance -> instance.group(
			typeCodec.optionalFieldOf(leftFieldName).forGetter(Pair::getFirst),
			typeCodec.optionalFieldOf(rightFieldName).forGetter(Pair::getSecond)
		).apply(instance, Pair::of)).comapFlatMap(pair -> {
			if (pair.getFirst().isEmpty() && pair.getSecond().isEmpty()) {
				return DataResult.error("Range object must have a minimum or maximum field");
			}
			return combineFunction.apply(pair.getFirst(), pair.getSecond());
		}, combined -> {
			return Pair.of(Optional.ofNullable(leftGetter.apply(combined)), Optional.ofNullable(rightGetter.apply(combined)));
		});

		return Codec.either(typeCodec, pairCodec).comapFlatMap(either -> {
			return either.map(constant -> {
				return combineFunction.apply(Optional.ofNullable(constant), Optional.ofNullable(constant));
			}, DataResult::success);
		}, combined -> {
			P first = leftGetter.apply(combined);
			P second = rightGetter.apply(combined);
			return Objects.equals(first, second) ? Either.left(first) : Either.right(combined);
		});
	}

	public static <A> MapCodec<A> optionalFieldOf(final Codec<A> codec, final String name, final A defaultValue) {
		return optionalFieldOf(codec, name).xmap(
				o -> o.orElse(defaultValue),
				a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a)
		);
	}

	public static <A> MapCodec<Optional<A>> optionalFieldOf(final Codec<A> codec, final String name) {
		return new NoCatchOptionalFieldCodec<A>(name, codec);
	}

	@SuppressWarnings("unchecked")
	public static <T> Codec<T> jsonSerializerCodec(Class<T> clazz, String elementName) {
		return Codec.PASSTHROUGH.flatXmap(to -> {
			try {
				JsonElement json = to.getValue() instanceof JsonElement ? (JsonElement)to.getValue() : ((DynamicOps<Object>)to.getOps()).convertTo(JsonOps.INSTANCE, to.getValue());
				T element = GSON.fromJson(json, clazz);
				return DataResult.success(element);
			} catch (JsonSyntaxException e) {
				InventoryActions.LOG.error("Error decoding {}", elementName, e);
				return DataResult.error(e.getMessage());
			}
		}, from -> {
			try {
				JsonElement json = GSON.toJsonTree(from);
				return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, json));
			} catch (JsonSyntaxException e) {
				InventoryActions.LOG.error("Error encoding {}", elementName, e);
				return DataResult.error(e.getMessage());
			}
		});
	}

	public static <T extends Enum<T>> Codec<T> enumCodec(Supplier<T[]> valuesSupplier, Function<T, String> toString) {
		Function<String, T> fromString = name -> {
			for (T value : valuesSupplier.get()) {
				if (toString.apply(value).contentEquals(name.toLowerCase(Locale.ROOT))) {
					return value;
				}
			}
			return null;
		};
		return enumCodec(valuesSupplier, fromString, toString);
	}

	public static <T extends Enum<T>> Codec<T> enumCodec(Supplier<T[]> valuesSupplier, Function<String, T> fromString, Function<T, String> toString) {
		return Codec.either(Codec.STRING, Codec.INT).comapFlatMap(either -> {
			return either.map(name -> {
				T value = fromString.apply(name);
				return value != null ? DataResult.success(value) : DataResult.error("Unknown enum value: " + name);
			}, index -> {
				T[] values = valuesSupplier.get();
				return index >= 0 && index < values.length ? DataResult.success(values[index]) : DataResult.error("Unknown enum value index: " + index);
			});
		}, value -> Either.left(toString.apply(value)));
	}

	public static Codec<ResourceLocation> namespacedResourceLocation(String defaultNamespace) {
		return Codec.STRING.comapFlatMap(str -> {
			try {
				return DataResult.success(new ResourceLocation(prefixLocation(defaultNamespace, str, ResourceLocation.NAMESPACE_SEPARATOR)));
			} catch (ResourceLocationException e) {
				return DataResult.error("Not a valid resource location: " + str + " " + e.getMessage());
			}
		}, ResourceLocation::toString).stable();
	}

	protected static String prefixLocation(String defaultNamespace, String location, char separator) {
		String[] parts = new String[] {defaultNamespace, location};
		int separatorIndex = location.indexOf(separator);

		if (separatorIndex >= 0) {
			parts[1] = location.substring(separatorIndex + 1);
			if (separatorIndex >= 1) {
				parts[0] = location.substring(0, separatorIndex);
			}
		}

		return StringUtils.join(parts, separator);
	}

	// Mojang's OptionalFieldCodec silently catches any errors, this version doesn't
	private static class NoCatchOptionalFieldCodec<A> extends OptionalFieldCodec<A> {
		private final String name;
	    private final Codec<A> elementCodec;

	    public NoCatchOptionalFieldCodec(final String name, final Codec<A> elementCodec) {
	        super(name, elementCodec);
	        this.name = name;
	        this.elementCodec = elementCodec;
	    }

	    @Override
	    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
	        final T value = input.get(this.name);
	        if (value == null) {
	            return DataResult.success(Optional.empty());
	        }

	        final DataResult<A> parsed = this.elementCodec.parse(ops, value);
	        if (parsed.error().isPresent()) {
	        	return DataResult.error(parsed.error().get().message());
	        } else if (parsed.result().isPresent()) {
	            return parsed.map(Optional::of);
	        }

	        return DataResult.success(Optional.empty());
	    }

	    @Override
	    public boolean equals(final Object o) {
	        if (this == o) {
	            return true;
	        } else if (o == null || this.getClass() != o.getClass()) {
	            return false;
	        }
	        final NoCatchOptionalFieldCodec<?> that = (NoCatchOptionalFieldCodec<?>) o;
	        return Objects.equals(this.name, that.name) && Objects.equals(this.elementCodec, that.elementCodec);
	    }
	}

	public static record SourceOrItem(Either<ItemSource, Holder<Item>> either) {
		public ItemStack get(ActionContext context) {
			return this.either.map(source -> source.get(context), ItemStack::new);
		}

		public static SourceOrItem ofSource(ItemSource source) {
			return new SourceOrItem(Either.left(source));
		}

		public static SourceOrItem ofItem(Holder<Item> item) {
			return new SourceOrItem(Either.right(item));
		}
	}

}
