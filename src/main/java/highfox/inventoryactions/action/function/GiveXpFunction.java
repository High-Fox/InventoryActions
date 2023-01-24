package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class GiveXpFunction implements IActionFunction {
	private final NumberProvider amountProvider;
	private final boolean addLevels;

	public GiveXpFunction(NumberProvider amount, boolean levels) {
		this.amountProvider = amount;
		this.addLevels = levels;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
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
		return ActionFunctionTypes.GIVE_XP.get();
	}

	public static class Deserializer implements IDeserializer<GiveXpFunction> {

		@Override
		public GiveXpFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			NumberProvider amountProvider = GsonHelper.getAsObject(json, "amount", context, NumberProvider.class);
			boolean addLevels = GsonHelper.getAsBoolean(json, "add_levels", false);

			return new GiveXpFunction(amountProvider, addLevels);
		}

		@Override
		public GiveXpFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}

}
