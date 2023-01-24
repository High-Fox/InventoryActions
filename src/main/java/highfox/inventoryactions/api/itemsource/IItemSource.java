package highfox.inventoryactions.api.itemsource;

import highfox.inventoryactions.api.action.IActionContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Holds a reference to the location of an item which can be retrieved and replaced
 */
public interface IItemSource {

	/**
	 * Gets the item from this source
	 *
	 * @param context the action context
	 * @return the item from this source
	 */
	ItemStack get(IActionContext context);

	/**
	 * Replaces the item in this source and broadcasts the changes
	 *
	 * @param context the action context
	 * @param stack the item to replace with
	 */
	void setAndUpdate(IActionContext context, ItemStack stack);

	/**
	 * Writes this source to a network buffer
	 *
	 * @param buffer the network buffer
	 */
	default void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeUtf(ItemSources.getName(this));
	}

}