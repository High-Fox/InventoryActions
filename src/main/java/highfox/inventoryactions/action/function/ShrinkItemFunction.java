package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class ShrinkItemFunction extends ItemSourcingFunction {
	public static final Codec<ShrinkItemFunction> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			UtilCodecs.optionalFieldOf(UtilCodecs.NUMBER_PROVIDER_CODEC, "amount", ConstantValue.exactly(1.0F)).forGetter(o -> o.amountProvider)
	).apply(instance, ShrinkItemFunction::new));

	private final NumberProvider amountProvider;

	public ShrinkItemFunction(ItemSource source, NumberProvider amount) {
		super(source);
		this.amountProvider = amount;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		ItemStack stack = this.source.get(context);
		int amount = this.amountProvider.getInt(context.getLootContext());

		workQueue.add(() -> {
			stack.shrink(amount);
			this.source.setAndUpdate(context, stack);
		});
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.SHRINK_ITEM.get();
	}

}
