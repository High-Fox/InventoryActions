package highfox.inventoryactions.action.function;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.ItemSourcingFunction;
import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.api.util.LootParams;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class ApplyModifiersFunction extends ItemSourcingFunction {
	private final LootItemFunction[] functions;
	private final LootParams params;

	public ApplyModifiersFunction(IItemSource source, LootItemFunction[] functions, LootParams params) {
		super(source);
		this.functions = functions;
		this.params = params;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
		LootContext lootContext = context.getLootContext(this.params.getTool(context), this.params.getBlockState());
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
				ActionsConstants.LOG.error("Error applying item function: {}", e.getMessage());
			}
		}
		return stack;
	}

	public static List<LootItemFunction> validateContextParams(List<LootItemFunction> functions) {
		Set<LootContextParam<?>> allowedParams = ActionContext.ACTION_PARAM_SET.getAllowed();

		return functions.stream().filter(function -> {
			Set<LootContextParam<?>> missingParams = Sets.difference(function.getReferencedContextParams(), allowedParams);

			if (!missingParams.isEmpty()) {
				ActionsConstants.LOG.warn("Cannot apply item function {} as it references missing params", BuiltInRegistries.LOOT_FUNCTION_TYPE.getId(function.getType()));
				return false;
			} else {
				return true;
			}
		}).toList();
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.APPLY_MODIFIERS.get();
	}

	public static class Deserializer extends BaseDeserializer<ApplyModifiersFunction> {

		@Override
		public ApplyModifiersFunction fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			LootItemFunction[] functions = context.deserialize(GsonHelper.getAsJsonArray(json, "functions"), LootItemFunction[].class);
			LootParams params = LootParams.fromJson(json);

			return new ApplyModifiersFunction(source, functions, params);
		}

	}

}
