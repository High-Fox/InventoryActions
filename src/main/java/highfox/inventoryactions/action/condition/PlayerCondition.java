package highfox.inventoryactions.action.condition;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ClientMethods;
import highfox.inventoryactions.util.NbtUtils;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class PlayerCondition implements IActionCondition {
	public static final Codec<PlayerCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UtilCodecs.optionalFieldOf(ResourceKey.codec(Registry.DIMENSION_REGISTRY), "dimension").forGetter(o -> o.dimension),
			UtilCodecs.optionalFieldOf(UtilCodecs.enumCodec(GameType::values, GameType::byName, GameType::getName), "game_mode").forGetter(o -> o.gameMode),
			UtilCodecs.optionalFieldOf(UtilCodecs.rangedCodec(ExtraCodecs.NON_NEGATIVE_INT, "minimum", "maximum", (min, max) -> {
				Integer minimum = min.orElse(Integer.valueOf(0));
				Integer maximum = max.orElse(Integer.MAX_VALUE);
				return minimum.compareTo(maximum) <= 0 ? DataResult.success(Pair.of(minimum, maximum)) : DataResult.error("Minimum must be less than or equal to maximum");
			}, Pair::getFirst, Pair::getSecond), "experience_level").forGetter(o -> o.experienceLevel),
			CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(o -> o.nbt)
	).apply(instance, PlayerCondition::new));

	private final Optional<ResourceKey<Level>> dimension;
	private final Optional<GameType> gameMode;
	private final Optional<Pair<Integer, Integer>> experienceLevel;
	private final Optional<CompoundTag> nbt;

	public PlayerCondition(Optional<ResourceKey<Level>> dimension, Optional<GameType> gameMode, Optional<Pair<Integer, Integer>> experienceLevel, Optional<CompoundTag> nbt) {
		this.dimension = dimension;
		this.gameMode = gameMode;
		this.experienceLevel = experienceLevel;
		this.nbt = nbt;
	}

	@Override
	public boolean test(ActionContext context) {
		Player player = context.getPlayer();

		if (this.dimension.isPresent() && !player.getLevel().dimension().equals(this.dimension.get())) {
			return false;
		}

		if (this.gameMode.isPresent()) {
			GameType gameType;
			if (player instanceof ServerPlayer serverPlayer) {
				gameType = serverPlayer.gameMode.getGameModeForPlayer();
			} else {
				gameType = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientMethods::getClientGameMode);
			}

			if (gameType != null && gameType != this.gameMode.get()) {
				return false;
			}
		}

		if (this.experienceLevel.isPresent() && (player.experienceLevel < this.experienceLevel.get().getFirst() || player.experienceLevel > this.experienceLevel.get().getSecond())) {
			return false;
		}

		if (this.nbt.isPresent()) {
			CompoundTag playerNbt = NbtPredicate.getEntityTagToCompare(player);
			if (!NbtUtils.compareNbt(this.nbt.get(), playerNbt)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.PLAYER.get();
	}

}
