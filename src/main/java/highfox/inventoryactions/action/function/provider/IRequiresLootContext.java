package highfox.inventoryactions.action.function.provider;

import java.util.Optional;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.mixin.LootContextParamSetsInvoker;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public interface IRequiresLootContext {
	public static final LootContextParamSet ACTION_PARAM_SET = LootContextParamSetsInvoker.invokeRegister(InventoryActions.MODID + ":inventory_action", builder -> {
		builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).optional(LootContextParams.TOOL).optional(LootContextParams.BLOCK_STATE);
	});

	static <T extends IRequiresLootContext> Products.P2<RecordCodecBuilder.Mu<T>, Optional<Either<ItemSource, IItemProvider>>, Optional<BlockState>> paramsCodec(RecordCodecBuilder.Instance<T> instance) {
		return instance.group(
				UtilCodecs.optionalFieldOf(Codec.either(ItemSource.CODEC, IItemProvider.CODEC), "tool").forGetter(IRequiresLootContext::getTool),
				UtilCodecs.optionalFieldOf(BlockState.CODEC, "block_state").forGetter(IRequiresLootContext::getBlockState)
		);
	}

	Optional<Either<ItemSource, IItemProvider>> getTool();
	Optional<BlockState> getBlockState();

	default ItemStack getToolStack(ActionContext context) {
		return this.getTool().map(either -> either.map(source -> source.get(context), provider -> {
			if (provider instanceof ISingleItemResult) {
				return ((ISingleItemResult)provider).getItem(context, context.getRandom());
			} else {
				ObjectArrayList<ItemStack> results = new ObjectArrayList<ItemStack>();
				provider.addItems(context, context.getRandom(), results);
				return results.get(0);
			}
		})).orElse(ItemStack.EMPTY);
	}
}
