package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class DamageItemFunction extends ItemSourcingFunction {
	public static final Codec<DamageItemFunction> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			UtilCodecs.NUMBER_PROVIDER_CODEC.optionalFieldOf("amount", ConstantValue.exactly(1.0F)).forGetter(o -> o.amountProvider)
	).apply(instance, DamageItemFunction::new));

	private final NumberProvider amountProvider;

	public DamageItemFunction(ItemSource source, NumberProvider amount) {
		super(source);
		this.amountProvider = amount;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		ItemStack stack = this.source.get(context);
		int amount = this.amountProvider.getInt(context.getLootContext());

		workQueue.add(() -> {
			stack.hurtAndBreak(amount, context.getPlayer(), player -> player.broadcastBreakEvent(LivingEntity.getEquipmentSlotForItem(stack)));
			this.source.setAndUpdate(context, stack);
		});
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.DAMAGE_ITEM.get();
	}

}
