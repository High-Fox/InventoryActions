package highfox.inventoryactions.action.condition;

import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.InventoryActionsClient;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.util.NbtUtils;
import highfox.inventoryactions.util.SerializationUtils;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DistExecutor;

public class PlayerCondition implements IActionCondition {
	private final Optional<ResourceKey<Level>> dimension;
	private final Optional<GameType> gameMode;
	private final Optional<MinMaxBounds.Ints> experienceLevel;
	private final Optional<CompoundTag> nbt;

	public PlayerCondition(Optional<ResourceKey<Level>> dimension, Optional<GameType> gameMode, Optional<MinMaxBounds.Ints> experienceLevel, Optional<CompoundTag> nbt) {
		this.dimension = dimension;
		this.gameMode = gameMode;
		this.experienceLevel = experienceLevel;
		this.nbt = nbt;
	}

	@Override
	public boolean test(IActionContext context) {
		Player player = context.getPlayer();

		if (this.dimension.isPresent() && !player.getLevel().dimension().equals(this.dimension.get())) {
			return false;
		}

		if (this.gameMode.isPresent()) {
			GameType gameType;
			if (player instanceof ServerPlayer) {
				gameType = ((ServerPlayer) player).gameMode.getGameModeForPlayer();
			} else {
				gameType = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> InventoryActionsClient::getClientGameMode);
			}

			if (gameType != null && gameType != this.gameMode.get()) {
				return false;
			}
		}

		if (this.experienceLevel.isPresent() && !this.experienceLevel.get().matches(player.experienceLevel)) {
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
		return ActionConditionTypes.PLAYER.get();
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeOptional(this.dimension, FriendlyByteBuf::writeResourceKey);
		buffer.writeOptional(this.gameMode, FriendlyByteBuf::writeEnum);
		buffer.writeOptional(this.experienceLevel, (buf, bounds) -> {
			buf.writeOptional(Optional.ofNullable(bounds.getMin()), FriendlyByteBuf::writeVarInt);
			buf.writeOptional(Optional.ofNullable(bounds.getMax()), FriendlyByteBuf::writeVarInt);
		});
		buffer.writeOptional(this.nbt, FriendlyByteBuf::writeNbt);
	}

	public static class Deserializer implements IDeserializer<PlayerCondition> {

		@Override
		public PlayerCondition fromJson(JsonObject json, JsonDeserializationContext context) {
			ResourceKey<Level> dimension = json.has("dimension") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(json, "dimension"))) : null;
			GameType gameMode = json.has("game_mode") ? GameType.byName(GsonHelper.getAsString(json, "game_mode")) : null;
			MinMaxBounds.Ints experienceLevel = json.has("experience_level") ? SerializationUtils.getAsIntRange(json, "experience_level") : null;
			CompoundTag nbt = json.has("nbt") ? CraftingHelper.getNBT(json.get("nbt")) : null;

			return new PlayerCondition(Optional.ofNullable(dimension), Optional.ofNullable(gameMode), Optional.ofNullable(experienceLevel), Optional.ofNullable(nbt));
		}

		@Override
		public PlayerCondition fromNetwork(FriendlyByteBuf buffer) {
			Optional<ResourceKey<Level>> dimension = buffer.readOptional(buf -> buf.readResourceKey(Registries.DIMENSION));
			Optional<GameType> gameMode = buffer.readOptional(buf -> buf.readEnum(GameType.class));
			Optional<MinMaxBounds.Ints> experienceLevel = buffer.readOptional(buf -> new MinMaxBounds.Ints(buf.readOptional(buf0 -> buf0.readVarInt()).orElse(null), buf.readOptional(buf0 -> buf0.readVarInt()).orElse(null)));
			Optional<CompoundTag> nbt = buffer.readOptional(buf -> buf.readAnySizeNbt());

			return new PlayerCondition(dimension, gameMode, experienceLevel, nbt);
		}

	}

}
