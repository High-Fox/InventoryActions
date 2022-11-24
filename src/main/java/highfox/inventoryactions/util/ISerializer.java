package highfox.inventoryactions.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.action.condition.IActionCondition;
import highfox.inventoryactions.action.function.IActionFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

// todo: experiment with using json adapters instead of codecs to maybe improve de/serialization speeds
public interface ISerializer<T> {
	T fromJson(JsonObject json, JsonDeserializationContext context);
	void toNetwork(T instance, FriendlyByteBuf buffer);
	T fromNetwork(FriendlyByteBuf buffer);

	public static class ActionSerializer implements ISerializer<InventoryAction> {
		@Override
		public InventoryAction fromJson(JsonObject json, JsonDeserializationContext context) {
			JsonArray conditionsArray = GsonHelper.getAsJsonArray(json, "conditions");
			IActionCondition[] conditions = new IActionCondition[conditionsArray.size()];
			for (int i = 0; i < conditions.length; i++) {
				JsonObject element = GsonHelper.convertToJsonObject(conditionsArray.get(i), "conditions[" + i + "]");
				conditions[i] = context.deserialize(element, IActionCondition.class);
			}

			JsonArray functionsArray = GsonHelper.getAsJsonArray(json, "conditions");
			IActionFunction[] functions = new IActionFunction[functionsArray.size()];
			for (int i = 0; i < functions.length; i++) {
				JsonObject element = GsonHelper.convertToJsonObject(functionsArray.get(i), "conditions[" + i + "]");
				functions[i] = context.deserialize(element, IActionFunction.class);
			}

			return new InventoryAction(ImmutableList.copyOf(conditions), ImmutableList.copyOf(functions));
		}

		@Override
		public void toNetwork(InventoryAction instance, FriendlyByteBuf buffer) {
		}

		@Override
		public InventoryAction fromNetwork(FriendlyByteBuf buffer) {
			return null;
		}
	}
}
