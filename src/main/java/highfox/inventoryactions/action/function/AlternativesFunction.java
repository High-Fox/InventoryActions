package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.util.SerializationUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class AlternativesFunction implements IActionFunction {
	private final List<Pair<List<IActionCondition>, List<IActionFunction>>> entries;

	public AlternativesFunction(List<Pair<List<IActionCondition>, List<IActionFunction>>> entries) {
		this.entries = entries;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {

		for (Pair<List<IActionCondition>, List<IActionFunction>> pair : this.entries) {
			if (pair.getFirst().stream().allMatch(condition -> condition.test(context))) {
				pair.getSecond().forEach(function -> function.run(workQueue, context));
				break;
			}
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.ALTERNATIVES.get();
	}

	public static class Deserializer implements IDeserializer<AlternativesFunction> {

		@Override
		public AlternativesFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			JsonArray entriesArray = GsonHelper.getAsJsonArray(json, "entries");
			ImmutableList.Builder<Pair<List<IActionCondition>, List<IActionFunction>>> builder = ImmutableList.builderWithExpectedSize(entriesArray.size());
			for (JsonElement element : entriesArray) {
				JsonObject object = GsonHelper.convertToJsonObject(element, "entry");
				List<IActionCondition> conditions = context.deserialize(GsonHelper.getAsJsonArray(object, "conditions"), SerializationUtils.CONDITION_LIST_TYPE);
				List<IActionFunction> functions = context.deserialize(GsonHelper.getAsJsonArray(object, "functions"), SerializationUtils.FUNCTION_LIST_TYPE);

				builder.add(Pair.of(conditions, functions));
			}

			return new AlternativesFunction(builder.build());
		}

		@Override
		public AlternativesFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}

}
