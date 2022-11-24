package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class GiveXpFunction implements IActionFunction {
	public static final Codec<GiveXpFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UtilCodecs.NUMBER_PROVIDER_CODEC.fieldOf("amount").forGetter(o -> o.amountProvider),
			Codec.BOOL.optionalFieldOf("add_levels", Boolean.valueOf(false)).forGetter(o -> o.addLevels)
	).apply(instance, GiveXpFunction::new));

	private final NumberProvider amountProvider;
	private final boolean addLevels;

	public GiveXpFunction(NumberProvider amount, boolean levels) {
		this.amountProvider = amount;
		this.addLevels = levels;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		Player player = context.getPlayer();
		int amount = this.amountProvider.getInt(context.getLootContext());

		if (this.addLevels) {
			workQueue.add(() -> player.giveExperienceLevels(amount));
		} else {
			workQueue.add(() -> {
				player.giveExperiencePoints(amount);
				player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.35F + 0.9F);
			});
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.GIVE_XP.get();
	}

}
