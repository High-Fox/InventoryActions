package highfox.inventoryactions.api.itemmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import highfox.inventoryactions.api.util.ActionsConstants;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registry of item maps that can be referenced by name
 */
public class BuiltInItemMaps {
	private static final Map<ResourceLocation, Supplier<ItemMap>> MAP_SUPPLIERS = new HashMap<>();
	private static final Object2ReferenceMap<ResourceLocation, ItemMap> MAPS = new Object2ReferenceOpenHashMap<>();
	private static final Reference2ObjectMap<ItemMap, ResourceLocation> MAP_NAMES = new Reference2ObjectOpenHashMap<>();

	static {
		register("strippables", () -> new ItemMap(fromBlockMap(AxeItem.STRIPPABLES)));
		register("oxidization_add", () -> new ItemMap(fromBlockMap(WeatheringCopper.NEXT_BY_BLOCK.get())));
		register("oxidization_remove", () -> new ItemMap(fromBlockMap(WeatheringCopper.PREVIOUS_BY_BLOCK.get())));
		register("wax_on", () -> new ItemMap(fromBlockMap(HoneycombItem.WAXABLES.get())));
		register("wax_off", () -> new ItemMap(fromBlockMap(HoneycombItem.WAX_OFF_BY_BLOCK.get())));
	}

	private static Supplier<ItemMap> register(String name, Supplier<ItemMap> supplier) {
		return register(new ResourceLocation(ActionsConstants.MODID, name), supplier);
	}

	/**
	 * Registers an item map with the given name
	 *
	 * @param name the corresponding name
	 * @param supplier a supplier that returns an item map
	 * @return the registered item map supplier
	 */
	public static Supplier<ItemMap> register(ResourceLocation name, Supplier<ItemMap> supplier) {
		MAP_SUPPLIERS.put(name, supplier);
		return supplier;
	}

	/**
	 * Returns the item map associated with the given name
	 *
	 * @param name the name
	 * @return the corresponding item map, or null if no item map was present for the given name
	 */
	@Nullable
	public static ItemMap getItemMap(ResourceLocation name) {
		if (!MAPS.containsKey(name) && MAP_SUPPLIERS.containsKey(name)) {
			ItemMap value = MAP_SUPPLIERS.get(name).get();
			MAPS.put(name, value);
			MAP_NAMES.put(value, name);
		}

		return MAPS.get(name);
	}

	/**
	 * Returns the name associated with the given item map
	 *
	 * @param map the item map
	 * @return the corresponding name, or null if the built-in registry does not contain the given map
	 */
	@Nullable
	public static ResourceLocation getName(ItemMap map) {
		return MAP_NAMES.get(map);
	}

	private static Map<ResourceLocation, ResourceLocation> fromBlockMap(Map<Block, Block> map) {
		Function<Block, ResourceLocation> mapper = block -> ForgeRegistries.ITEMS.getKey(block.asItem());
		return map.entrySet()
				.stream()
				.map(mapEntry(mapper, mapper))
				.collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
	}

	private static <A, B, C, D> Function<Entry<A, B>, Entry<C, D>> mapEntry(Function<A, C> keyMapper, Function<B, D> valueMapper) {
		return entry -> Map.entry(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue()));
	}

}
