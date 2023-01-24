package highfox.inventoryactions.action.condition;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.util.SerializationUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemCondition extends ItemSourcingCondition {
	private final List<ResourceLocation> items;
	private final Optional<Pattern> namespacePattern;
	private final Predicate<String> namespacePredicate;
	private final Optional<Pattern> pathPattern;
	private final Predicate<String> pathPredicate;

	public ItemCondition(IItemSource source, List<ResourceLocation> items, Optional<Pattern> namespacePattern, Optional<Pattern> pathPattern) {
		super(source);
		this.items = items;
		this.namespacePattern = namespacePattern;
		this.namespacePredicate = this.namespacePattern.map(Pattern::asPredicate).orElse(str -> true);
		this.pathPattern = pathPattern;
		this.pathPredicate = this.pathPattern.map(Pattern::asPredicate).orElse(str -> true);
	}

	@Override
	public boolean test(IActionContext context) {
		ItemStack stack = this.source.get(context);
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

		if (!this.items.isEmpty() && !this.items.stream().anyMatch(id::equals)) {
			return false;
		} else if (id != null) {
			if (!this.namespacePredicate.test(id.getNamespace()) || !this.pathPredicate.test(id.getPath())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.ITEM.get();
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		buffer.writeCollection(this.items, FriendlyByteBuf::writeResourceLocation);
		buffer.writeOptional(this.namespacePattern, (buf, pattern) -> buf.writeUtf(pattern.pattern()));
		buffer.writeOptional(this.pathPattern, (buf, pattern) -> buf.writeUtf(pattern.pattern()));
	}

	public static class Deserializer extends BaseDeserializer<ItemCondition> {

		@Override
		public ItemCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			List<ResourceLocation> items = Optional.ofNullable(GsonHelper.getAsJsonArray(json, "items", null)).map(itemNames -> {
				ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builderWithExpectedSize(itemNames.size());
				for (JsonElement element : itemNames) {
					ResourceLocation name = new ResourceLocation(GsonHelper.convertToString(element, "item"));
					if (!ForgeRegistries.ITEMS.containsKey(name)) {
						throw new JsonSyntaxException("Unknown item: " + name);
					} else {
						builder.add(name);
					}
				}

				return builder.build();
			}).orElse(ImmutableList.of());

			Optional<Pattern> namespacePattern = Optional.ofNullable(SerializationUtils.getAsPattern(json, "namespace", null));
			Optional<Pattern> pathPattern = Optional.ofNullable(SerializationUtils.getAsPattern(json, "path", null));

			return new ItemCondition(source, items, namespacePattern, pathPattern);
		}

		@Override
		public ItemCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			List<ResourceLocation> items = buffer.readList(FriendlyByteBuf::readResourceLocation);
			Optional<Pattern> namespacePattern = buffer.readOptional(buf -> Pattern.compile(buf.readUtf()));
			Optional<Pattern> pathPattern = buffer.readOptional(buf -> Pattern.compile(buf.readUtf()));

			return new ItemCondition(source, ImmutableList.copyOf(items), namespacePattern, pathPattern);
		}

	}

}
