package highfox.inventoryactions.action;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.action.condition.ActionConditionTypes;
import highfox.inventoryactions.action.function.ActionFunctionTypes;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.util.SerializationUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;;

public class InventoryAction {
	private final List<IActionCondition> conditions;
	private final IActionFunction[] functions;
	private final Queue<Runnable> workQueue = Queues.newLinkedBlockingQueue();

	public InventoryAction(List<IActionCondition> conditions, IActionFunction[] functions) {
		this.conditions = conditions;
		this.functions = functions;
	}

	public boolean canRunAction(IActionContext context) {
		Player player = context.getPlayer();

		return player != null
				&& !context.getTarget().isEmpty()
				&& !context.getUsing().isEmpty()
				&& context.getSlot().allowModification(player)
				&& this.conditions.stream().allMatch(condition -> {
					try {
						return condition.test(context);
					} catch (Exception e) {
						ActionsConstants.LOG.error("Error checking condition {}", ActionConditionTypes.getRegistryName(condition.getType()));
						return false;
					}
				});
	}

	public void runAction(IActionContext context) {
		if (this.functions.length > 0 && !context.getLevel().isClientSide()) {
			for (int i = 0; i < this.functions.length; i++) {
				IActionFunction function = this.functions[i];
				try {
					function.run(this.workQueue, context);
				} catch (Exception e) {
					ActionsConstants.LOG.error("Error running function {}", ActionFunctionTypes.getRegistryName(function.getType()));
				}
			}

			while (!this.workQueue.isEmpty()) {
				try {
					this.workQueue.poll().run();
				} catch (Exception e) {
					ActionsConstants.LOG.error("Error running inventory action", e);
				}
			}
		}
	}

	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.conditions.size());
		for (IActionCondition condition : this.conditions) {
			buffer.writeResourceLocation(ActionConditionTypes.getRegistryName(condition.getType()));
			condition.toNetwork(buffer);
		}
	}

	public static InventoryAction fromNetwork(FriendlyByteBuf buffer) {
		int size = buffer.readVarInt();
		ImmutableList.Builder<IActionCondition> builder = ImmutableList.builderWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			ActionConditionType type = ActionConditionTypes.CONDITION_SERIALIZERS.get().getValue(buffer.readResourceLocation());
			builder.add(type.getDeserializer().fromNetwork(buffer));
		}

		return new InventoryAction(builder.build(), new IActionFunction[0]);
	}

	public static class Deserializer implements JsonDeserializer<InventoryAction> {

		@Override
		public InventoryAction deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject object = GsonHelper.convertToJsonObject(json, "inventory action");
			List<IActionCondition> conditions = context.deserialize(GsonHelper.getAsJsonArray(object, "conditions"), SerializationUtils.CONDITION_LIST_TYPE);
			if (conditions.isEmpty()) {
				throw new JsonSyntaxException("Inventory action must have at least 1 condition");
			}
			IActionFunction[] functions = GsonHelper.getAsObject(object, "functions", new IActionFunction[0], context, IActionFunction[].class);

			return new InventoryAction(conditions, functions);
		}

	}



}