package highfox.inventoryactions.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import highfox.inventoryactions.InventoryActions;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemMap {
	public static final Codec<Map<Holder<Item>, Holder<Item>>> DIRECT_CODEC = Codec.unboundedMap(RegistryFileCodec.create(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS.getCodec()), RegistryFileCodec.create(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS.getCodec()));
	public static final Codec<ResourceLocation> REFERENCE_CODEC = UtilCodecs.namespacedResourceLocation(InventoryActions.MODID).comapFlatMap(location -> {
		if (!DefaultItemMaps.containsKey(location)) {
			return DataResult.error("Unknown item map: " + location);
		} else {
			return DataResult.success(location);
		}
	}, Function.identity());
	public static final Codec<ItemMap> CODEC = Codec.either(REFERENCE_CODEC, DIRECT_CODEC).xmap(either -> {
		return either.map(DefaultItemMaps::getValue, ItemMap::new);
	}, map -> DefaultItemMaps.containsValue(map) ? Either.left(DefaultItemMaps.getKey(map)) : Either.right(map.map()));

	private final Reference2ReferenceMap<Holder<Item>, Holder<Item>> values;

	public ItemMap(Map<Holder<Item>, Holder<Item>> map) {
		this.values = Reference2ReferenceMaps.unmodifiable(new Reference2ReferenceOpenHashMap<Holder<Item>, Holder<Item>>(map));
	}

	@Nullable
	public Holder<Item> getValue(Holder<Item> key) {
		return this.values.get(key);
	}

	public boolean containsKey(Holder<Item> key) {
		return this.values.containsKey(key);
	}

	public boolean containsValue(Holder<Item> value) {
		return this.values.containsValue(value);
	}

	public Reference2ReferenceMap<Holder<Item>, Holder<Item>> map() {
		return this.values;
	}

	public int size() {
		return this.values.size();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		ItemMap other = (ItemMap) obj;
		return Objects.equals(this.values, other.values);
	}

	public synchronized void write(FriendlyByteBuf buf) {
		BiConsumer<FriendlyByteBuf, Holder<Item>> writer = (buff, holder) -> buff.writeResourceLocation(holder.unwrap().orThrow().location());

		buf.writeMap(this.map(), writer, writer);
	}

	public static ItemMap read(FriendlyByteBuf buf) {
		Function<FriendlyByteBuf, Holder<Item>> reader = buff -> ForgeRegistries.ITEMS.getHolder(buff.readResourceLocation()).orElseThrow();
		Map<Holder<Item>, Holder<Item>> map = buf.readMap(reader, reader);

		return new ItemMap(map);
	}

}
