package highfox.inventoryactions.action.function.provider;

import java.util.Random;

import highfox.inventoryactions.action.ActionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;

public interface ISingleItemResult extends IItemProvider {
	@Override
	default void addItems(ActionContext context, Random random, ObjectArrayList<ItemStack> results) {
		results.add(this.getItem(context, random));
	}

	ItemStack getItem(ActionContext context, Random random);

}
