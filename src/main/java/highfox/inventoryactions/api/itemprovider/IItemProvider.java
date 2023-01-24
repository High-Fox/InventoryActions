package highfox.inventoryactions.api.itemprovider;

import highfox.inventoryactions.api.action.IActionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface IItemProvider {

	/**
	 * Generates this provider's items and adds them to a list
	 *
	 * @param context the context this provider is being run from
	 * @param random a threaded random source
	 * @param results a list of resulting items
	 */
	void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results);

	/**
	 * Gets the deserializer type
	 *
	 * @return a {@link ItemProviderType}
	 */
	ItemProviderType getType();
}
