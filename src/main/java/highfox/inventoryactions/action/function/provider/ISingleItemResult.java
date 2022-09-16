package highfox.inventoryactions.action.function.provider;

import highfox.inventoryactions.action.ActionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface ISingleItemResult extends IItemProvider {
	@Override
	default void addItems(ActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		results.add(this.getItem(context, random));
	}

	ItemStack getItem(ActionContext context, RandomSource random);

}
