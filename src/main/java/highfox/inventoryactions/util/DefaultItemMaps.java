package highfox.inventoryactions.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import highfox.inventoryactions.InventoryActions;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraftforge.registries.ForgeRegistries;

public class DefaultItemMaps {
	public static final Supplier<BiMap<ResourceLocation, ItemMap>> MAPS_BY_NAME = Suppliers.memoize(() -> Util.make(ImmutableBiMap.<ResourceLocation, ItemMap>builder(), builder -> {
			add(builder, "strippables", new ItemMap(fromBlockMap(AxeItem.STRIPPABLES)));
			add(builder, "oxidization_add",  new ItemMap(fromBlockMap(WeatheringCopper.NEXT_BY_BLOCK.get())));
			add(builder, "oxidization_remove",  new ItemMap(fromBlockMap(WeatheringCopper.PREVIOUS_BY_BLOCK.get())));
			add(builder, "wax_on", new ItemMap(fromBlockMap(HoneycombItem.WAXABLES.get())));
			add(builder, "wax_off", new ItemMap(fromBlockMap(HoneycombItem.WAX_OFF_BY_BLOCK.get())));
		}).build());
	public static final Supplier<BiMap<ItemMap, ResourceLocation>> NAMES_BY_MAP = Suppliers.memoize(() -> MAPS_BY_NAME.get().inverse());

	@Nullable
	public static ItemMap getValue(ResourceLocation name) {
		return MAPS_BY_NAME.get().get(name);
	}

	@Nullable
	public static ResourceLocation getKey(ItemMap map) {
		return NAMES_BY_MAP.get().get(map);
	}

	public static boolean containsKey(ResourceLocation name) {
		return MAPS_BY_NAME.get().containsKey(name);
	}

	public static boolean containsValue(ItemMap map) {
		return MAPS_BY_NAME.get().containsValue(map);
	}

	private static void add(ImmutableBiMap.Builder<ResourceLocation, ItemMap> builder, String name, ItemMap itemMap) {
		builder.put(new ResourceLocation(InventoryActions.MODID, name), itemMap);
	}

	private static Map<Holder<Item>, Holder<Item>> fromBlockMap(Map<Block, Block> map) {
		return map.entrySet()
				.stream()
				.map(entry -> mapEntry(entry, DefaultItemMaps::itemHolderFromBlock))
				.collect(ImmutableBiMap.toImmutableBiMap(Entry::getKey, Entry::getValue));
	}

	private static <A, B> Entry<B, B> mapEntry(Entry<A, A> entry, Function<A, B> mapper) {
		return Map.entry(mapper.apply(entry.getKey()), mapper.apply(entry.getValue()));
	}

	private static Holder<Item> itemHolderFromBlock(Block block) {
		return ForgeRegistries.ITEMS.getHolder(block.asItem()).orElseThrow();
	}

}
