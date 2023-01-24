package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.util.SerializationUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class ConditionalFunction implements IActionFunction {
	private final IActionFunction function;
	private final List<IActionCondition> conditions;

	public ConditionalFunction(IActionFunction function, List<IActionCondition> conditions) {
		this.function = function;
		this.conditions = conditions;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
		if (this.conditions.stream().allMatch(condition -> condition.test(context))) {
			this.function.run(workQueue, context);
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.CONDITIONAL.get();
	}

	public static class Deserializer implements IDeserializer<ConditionalFunction> {

		@Override
		public ConditionalFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			IActionFunction function = GsonHelper.getAsObject(json, "function", context, IActionFunction.class);
			List<IActionCondition> conditions = context.deserialize(GsonHelper.getAsJsonArray(json, "conditions"), SerializationUtils.CONDITION_LIST_TYPE);

			return new ConditionalFunction(function, conditions);
		}

		@Override
		public ConditionalFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}

}
