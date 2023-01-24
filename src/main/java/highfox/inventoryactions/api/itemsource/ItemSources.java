package highfox.inventoryactions.api.itemsource;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.action.IActionContext;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Item source registry
 */
public class ItemSources {
	private static final Object2ReferenceMap<String, IItemSource> SOURCES = new Object2ReferenceOpenHashMap<>();
	private static final Reference2ObjectMap<IItemSource, String> SOURCE_NAMES = new Reference2ObjectOpenHashMap<>();

	static {
		register("action_target", IActionContext::getTarget, (context, stack) -> context.getSlot().set(stack));
		register("action_using", IActionContext::getUsing, (context, stack) -> context.getPlayer().containerMenu.setCarried(stack));
		IntStream.range(0, 9).forEach(slot -> register("player.hotbar." + slot, SlotSource.player(slot)));
		IntStream.range(0, 27).forEach(slot -> register("player.inventory." + slot, SlotSource.player(slot + 9)));
		register("player.weapon.mainhand", SlotSource.player(EquipmentSlot.MAINHAND.getIndex(98)));
		register("player.weapon.offhand", SlotSource.player(EquipmentSlot.OFFHAND.getIndex(98)));
		register("player.armor.head", SlotSource.player(EquipmentSlot.HEAD.getIndex(100)));
		register("player.armor.chest", SlotSource.player(EquipmentSlot.CHEST.getIndex(100)));
		register("player.armor.legs", SlotSource.player(EquipmentSlot.LEGS.getIndex(100)));
		register("player.armor.feet", SlotSource.player(EquipmentSlot.FEET.getIndex(100)));
	}

	/**
	 * Register a basic item source with the given name who's {@link IItemSource#get(IActionContext)} and
	 * {@link IItemSource#setAndUpdate(IActionContext, ItemStack)} methods call the given getter and setter, respectively
	 *
	 * @param name the corresponding name
	 * @param getter a function that retrieves the item
	 * @param setter a bi consumer that sets the given item and broadcasts changes
	 * @return the registered item source
	 */
	public static IItemSource register(String name, Function<IActionContext, ItemStack> getter, BiConsumer<IActionContext, ItemStack> setter) {
		return register(name, new IItemSource() {
			@Override
			public ItemStack get(IActionContext context) {
				return getter.apply(context);
			}

			@Override
			public void setAndUpdate(IActionContext context, ItemStack stack) {
				setter.accept(context, stack);
			}
		});
	}


	/**
	 * Register an item source with the given name
	 *
	 * @param name the corresponding name
	 * @param source the item source to register
	 * @return the registered item source
	 */
	public static IItemSource register(String name, IItemSource source) {
		SOURCES.put(name, source);
		SOURCE_NAMES.put(source, name);
		return source;
	}

	/**
	 * Gets the item source corresponding to the given name
	 *
	 * @param name the name
	 * @return the corresponding item source, or null if one doesn't exist
	 */
	@Nullable
	public static IItemSource getSource(String name) {
		return SOURCES.get(name);
	}

	/**
	 * Gets the name corresponding to the given item source
	 *
	 * @param source the item source
	 * @return the item source's corresponding name, or null if the item source isn't registered
	 */
	@Nullable
	public static String getName(IItemSource source) {
		return SOURCE_NAMES.get(source);
	}

	/**
	 * Checks if there is an item source registered to the given name
	 *
	 * @param name the name
	 * @return true if the name corresponds to a registered item source
	 */
	public static boolean isValidSource(String name) {
		return SOURCES.containsKey(name);
	}

	/**
	 * Reads an item source from a network buffer
	 *
	 * @param buffer the network buffer
	 * @return the item source
	 */
	public static IItemSource fromNetwork(FriendlyByteBuf buffer) {
		return getSource(buffer.readUtf());
	}

	/**
	 * Converts a json element to a registered item source
	 *
	 * @param json the json element to deserialize
	 * @return the deserialized item source
	 * @throws JsonSyntaxException if the json element is null, empty, or not a string
	 * @throws IllegalArgumentException if no item source with the given name is registered
	 */
	public static IItemSource fromJson(JsonElement json) {
		if (json == null || json.isJsonNull()) {
			throw new JsonSyntaxException("Missing item source, expected to find a string");
		} else if (json.isJsonPrimitive()) {
			String name = json.getAsString();
			if (isValidSource(name)) {
				return getSource(name);
			} else {
				throw new IllegalArgumentException("Unknown item source: " + name);
			}
		} else {
			throw new JsonSyntaxException("Expected item source to be a string, was " + GsonHelper.getType(json));
		}
	}

}
