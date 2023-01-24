package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.ItemSourcingFunction;
import highfox.inventoryactions.api.itemsource.IItemSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ShrinkItemFunction extends ItemSourcingFunction {
	private final NumberProvider amountProvider;

	public ShrinkItemFunction(IItemSource source, NumberProvider amountProvider) {
		super(source);
		this.amountProvider = amountProvider;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
		ItemStack stack = this.source.get(context);
		int amount = this.amountProvider.getInt(context.getLootContext());

		workQueue.add(() -> {
			stack.shrink(amount);
			this.source.setAndUpdate(context, stack);
		});
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.SHRINK_ITEM.get();
	}

	public static class Deserializer extends BaseDeserializer<ShrinkItemFunction> {

		@Override
		public ShrinkItemFunction fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			NumberProvider amountProvider = GsonHelper.getAsObject(json, "amount", ConstantValue.exactly(1.0F), context, NumberProvider.class);

			return new ShrinkItemFunction(source, amountProvider);
		}

	}

}
