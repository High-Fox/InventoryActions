package highfox.inventoryactions.action.condition;

import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class OrCondition implements IActionCondition {
	private final List<IActionCondition> conditions;

	public OrCondition(List<IActionCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(IActionContext context) {
		return this.conditions.stream().anyMatch(condition -> condition.test(context));
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.OR.get();
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeCollection(this.conditions, (buf, condition) -> {
			buf.writeResourceLocation(ActionConditionTypes.getRegistryName(condition.getType()));
			condition.toNetwork(buf);
		});
	}

	public static class Deserializer implements IDeserializer<OrCondition> {

		@Override
		public OrCondition fromJson(JsonObject json, JsonDeserializationContext context) {
			IActionCondition[] conditions = GsonHelper.getAsObject(json, "conditions", context, IActionCondition[].class);
			return new OrCondition(List.of(conditions));
		}

		@Override
		public OrCondition fromNetwork(FriendlyByteBuf buffer) {
			List<IActionCondition> conditions = buffer.readList(buf -> {
				ResourceLocation type = buf.readResourceLocation();
				return ActionConditionTypes.CONDITION_SERIALIZERS.get().getValue(type).getDeserializer().fromNetwork(buf);
			});

			return new OrCondition(conditions);
		}

	}

}
