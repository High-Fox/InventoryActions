package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundFunction implements IActionFunction {
	public static final Codec<PlaySoundFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryFileCodec.create(ForgeRegistries.Keys.SOUND_EVENTS, ForgeRegistries.SOUND_EVENTS.getCodec()).fieldOf("sound").forGetter(o -> o.sound),
			UtilCodecs.optionalFieldOf(UtilCodecs.enumCodec(SoundSource::values, SoundSource::getName), "category", SoundSource.PLAYERS).forGetter(o -> o.category),
			UtilCodecs.optionalFieldOf(ExtraCodecs.POSITIVE_FLOAT, "volume", 1.0F).forGetter(o -> o.volume),
			UtilCodecs.optionalFieldOf(ExtraCodecs.POSITIVE_FLOAT, "pitch", 1.0F).forGetter(o -> o.pitch)
	).apply(instance, PlaySoundFunction::new));

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
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		Player player = context.getPlayer();

		workQueue.add(() -> {
			player.getLevel().playSound((Player)null, player.getX(), player.getY(), player.getZ(), this.sound.value(), this.category, this.volume, Mth.clamp(this.pitch, 0.0F, 2.0F));
		});
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.PLAY_SOUND.get();
	}

}
