package highfox.inventoryactions.action.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class InvertCondition implements IActionCondition {
	private final IActionCondition condition;

	public InvertCondition(IActionCondition condition) {
		this.condition = condition;
	}

	@Override
	public boolean test(IActionContext context) {
		return !this.condition.test(context);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.INVERT.get();
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(ActionConditionTypes.getRegistryName(this.condition.getType()));
		this.condition.toNetwork(buffer);
	}

	public static class Deserializer implements IDeserializer<InvertCondition> {

		@Override
		public InvertCondition fromJson(JsonObject json, JsonDeserializationContext context) {
			IActionCondition condition = GsonHelper.getAsObject(json, "condition", context, IActionCondition.class);
			return new InvertCondition(condition);
		}

		@Override
		public InvertCondition fromNetwork(FriendlyByteBuf buffer) {
			ResourceLocation type = buffer.readResourceLocation();
			IActionCondition condition = ActionConditionTypes.CONDITION_SERIALIZERS.get().getValue(type).getDeserializer().fromNetwork(buffer);
			return new InvertCondition(condition);
		}

	}

}
