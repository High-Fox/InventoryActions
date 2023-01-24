package highfox.inventoryactions.action.function;

import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundFunction implements IActionFunction {
	private final Holder<SoundEvent> sound;
	private final SoundSource category;
	private final float volume;
	private final float pitch;

	public PlaySoundFunction(Holder<SoundEvent> sound, SoundSource category, float volume, float pitch) {
		this.sound = sound;
		this.category = category;
		this.volume = volume;
		this.pitch = pitch;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
		Player player = context.getPlayer();

		workQueue.add(() -> {
			player.getLevel().playSound((Player)null, player.getX(), player.getY(), player.getZ(), this.sound.get(), this.category, this.volume, this.pitch);
		});
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.PLAY_SOUND.get();
	}

	public static class Deserializer implements IDeserializer<PlaySoundFunction> {
		private static final Supplier<SoundSource[]> SOUND_SOURCES = Suppliers.memoize(SoundSource::values);

		@Override
		public PlaySoundFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			ResourceLocation soundName = new ResourceLocation(GsonHelper.getAsString(json, "sound"));
			Reference<SoundEvent> sound = ForgeRegistries.SOUND_EVENTS.getDelegate(soundName).orElseThrow(() -> {
				return new IllegalArgumentException("Unknown sound event: " + soundName);
			});
			SoundSource source = Optional.ofNullable(GsonHelper.getAsString(json, "category", null)).map(sourceName -> {
				for (SoundSource value : SOUND_SOURCES.get()) {
					if (value.getName().contentEquals(sourceName)) {
						return value;
					}
				}

				throw new IllegalArgumentException("Unknown sound category: " + sourceName);
			}).orElse(SoundSource.PLAYERS);

			float volume = GsonHelper.getAsFloat(json, "volume", 1.0F);
			float pitch = GsonHelper.getAsFloat(json, "pitch", 1.0F);

			return new PlaySoundFunction(sound, source, volume, pitch);
		}

		@Override
		public PlaySoundFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}

}
