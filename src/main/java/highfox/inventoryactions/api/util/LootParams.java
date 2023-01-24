package highfox.inventoryactions.api.util;

import java.util.Optional;

import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.api.itemsource.ItemSources;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.CraftingHelper;

public class LootParams {
	private final Optional<IItemSource> toolParam;
	private final Optional<BlockState> blockStateParam;

	public LootParams(Optional<IItemSource> toolParam, Optional<BlockState> blockStateParam) {
		this.toolParam = toolParam;
		this.blockStateParam = blockStateParam;
	}

	public ItemStack getTool(IActionContext context) {
		return this.toolParam.map(source -> source.get(context)).orElse(ItemStack.EMPTY);
	}

	public BlockState getBlockState() {
		return this.blockStateParam.orElse(null);
	}

	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeOptional(this.toolParam, (buf, source) -> {
			buf.writeUtf(ItemSources.getName(source));
		});
		buffer.writeOptional(this.blockStateParam, (buf, blockState) -> {
			buf.writeNbt(NbtUtils.writeBlockState(blockState));
		});
	}

	public static LootParams fromNetwork(FriendlyByteBuf buffer) {
		Optional<IItemSource> tool = buffer.readOptional(ItemSources::fromNetwork);
		Optional<BlockState> blockState = buffer.readOptional(buf -> {
			return NbtUtils.readBlockState(buf.readAnySizeNbt());
		});

		return new LootParams(tool, blockState);
	}

	public static LootParams fromJson(JsonObject json) {
		Optional<IItemSource> tool = Optional.ofNullable(json.has("tool") ? ItemSources.fromJson(json.get("tool")) : null);
		Optional<BlockState> blockState = Optional.ofNullable(json.has("block_state") ? CraftingHelper.getNBT(GsonHelper.getAsJsonObject(json, "block_state")) : null).map(nbt -> {
			return NbtUtils.readBlockState(nbt);
		});

		return new LootParams(tool, blockState);
	}
}
