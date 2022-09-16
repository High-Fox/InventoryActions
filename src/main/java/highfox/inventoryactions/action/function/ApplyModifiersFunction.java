package highfox.inventoryactions.action.function;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.function.provider.IItemProvider;
import highfox.inventoryactions.action.function.provider.IRequiresLootContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class ApplyModifiersFunction extends ItemSourcingFunction implements IRequiresLootContext {
	public static final Codec<ApplyModifiersFunction> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			UtilCodecs.optionalFieldOf(UtilCodecs.ITEM_FUNCTIONS_CODEC, "functions", new LootItemFunction[0]).forGetter(o -> o.functions)
	).and(IRequiresLootContext.paramsCodec(instance)).apply(instance, ApplyModifiersFunction::new));

	private final LootItemFunction[] functions;
	private final Optional<Either<ItemSource, IItemProvider>> tool;
	private final Optional<BlockState> blockState;

	public ApplyModifiersFunction(ItemSource source, LootItemFunction[] functions, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState) {
		super(source);
		this.functions = functions;
		this.tool = tool;
		this.blockState = blockState;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		LootContext lootContext = context.getLootContext(this.getToolStack(context), this.blockState.orElse(null));
		ItemStack stack = this.source.get(context);
		List<LootItemFunction> validFunctions = validateContextParams(Arrays.asList(this.functions));

		workQueue.add(() -> {
			this.source.setAndUpdate(context, applyModifiers(stack, lootContext, validFunctions));
		});
	}

	public static ItemStack applyModifiers(ItemStack stack, LootContext lootContext, List<LootItemFunction> functions) {
		for (LootItemFunction function : functions) {
			try {
				stack = function.apply(stack, lootContext);
			} catch (Exception e) {
				InventoryActions.LOG.error("Error applying item function: {}", e.getMessage());
			}
		}
		return stack;
	}

	public static List<LootItemFunction> validateContextParams(List<LootItemFunction> functions) {
		Set<LootContextParam<?>> allowedParams = IRequiresLootContext.ACTION_PARAM_SET.getAllowed();

		return functions.stream().filter(function -> {
			Set<LootContextParam<?>> missingParams = Sets.difference(function.getReferencedContextParams(), allowedParams);

			if (!missingParams.isEmpty()) {
				InventoryActions.LOG.warn("Cannot apply item function {} as it references missing params", Registry.LOOT_FUNCTION_TYPE.getId(function.getType()));
				return false;
			} else {
				return true;
			}
		}).toList();
	}

	@Override
	public Optional<Either<ItemSource, IItemProvider>> getTool() {
		return this.tool;
	}

	@Override
	public Optional<BlockState> getBlockState() {
		return this.blockState;
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.APPLY_MODIFIERS.get();
	}



}
