package highfox.inventoryactions.action.function.provider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.function.ApplyModifiersFunction;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public abstract class ItemFunctionsProvider implements IItemProvider, IRequiresLootContext {
	protected final LootItemFunction[] functions;
	protected final Optional<Either<ItemSource, IItemProvider>> tool;
	protected final Optional<BlockState> blockState;

	public ItemFunctionsProvider(LootItemFunction[] functions, Optional<Either<ItemSource, IItemProvider>> toolParam, Optional<BlockState> blockState) {
		this.functions = functions;
		this.tool = toolParam;
		this.blockState = blockState;
	}

	protected ItemStack applyModifiers(ActionContext context, ItemStack stack) {
		LootContext lootContext = context.getLootContext(this.getToolStack(context), this.blockState.orElse(null));
		List<LootItemFunction> validFunctions = ApplyModifiersFunction.validateContextParams(Arrays.asList(this.functions));

		return ApplyModifiersFunction.applyModifiers(stack, lootContext, validFunctions);
	}

	@Override
	public Optional<Either<ItemSource, IItemProvider>> getTool() {
		return this.tool;
	}

	@Override
	public Optional<BlockState> getBlockState() {
		return this.blockState;
	}

	protected static <T extends ItemFunctionsProvider> Products.P3<RecordCodecBuilder.Mu<T>, LootItemFunction[], Optional<Either<ItemSource, IItemProvider>>, Optional<BlockState>> functionsCodec(RecordCodecBuilder.Instance<T> instance) {
		return instance.group(
				UtilCodecs.ITEM_FUNCTIONS_CODEC.fieldOf("functions").orElseGet(() -> new LootItemFunction[0]).forGetter(o -> o.functions)
		).and(IRequiresLootContext.paramsCodec(instance));
	}

}