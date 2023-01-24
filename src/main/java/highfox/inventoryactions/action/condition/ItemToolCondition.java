package highfox.inventoryactions.action.condition;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemsource.IItemSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class ItemToolCondition extends ItemSourcingCondition {
	private final ToolType toolType;

	public ItemToolCondition(IItemSource source, ToolType toolType) {
		super(source);
		this.toolType = toolType;
	}

	@Override
	public boolean test(IActionContext context) {
		ItemStack stack = this.source.get(context);

		return this.toolType.matches(stack);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.TOOL.get();
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.toolType.getName());
	}

	private static enum ToolType {
		SWORD("sword", ToolActions.DEFAULT_SWORD_ACTIONS),
		SHIELD("shield", ToolActions.DEFAULT_SHIELD_ACTIONS),
		PICKAXE("pickaxe", ToolActions.DEFAULT_PICKAXE_ACTIONS),
		AXE("axe", ToolActions.DEFAULT_AXE_ACTIONS),
		HOE("hoe", ToolActions.DEFAULT_HOE_ACTIONS),
		SHOVEL("shovel", ToolActions.DEFAULT_SHOVEL_ACTIONS),
		SHEARS("shears", ToolActions.DEFAULT_SHEARS_ACTIONS);

		private static final ToolType[] VALUES = values();
		private final String name;
		private final Set<ToolAction> toolActions;

		private ToolType(String name, Set<ToolAction> toolActions) {
			this.name = name;
			this.toolActions = toolActions;
		}

		public String getName() {
			return this.name;
		}

		public boolean matches(ItemStack stack) {
			return this.toolActions.stream().allMatch(stack::canPerformAction);
		}

		@Nullable
		public static ToolType byName(String str) {
			for (ToolType toolType : VALUES) {
				if (str.contentEquals(toolType.getName())) {
					return toolType;
				}
			}

			return null;
		}
	}

	public static class Deserializer extends BaseDeserializer<ItemToolCondition> {

		@Override
		public ItemToolCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			String toolName = GsonHelper.getAsString(json, "tool");
			ToolType toolType = ToolType.byName(toolName);
			if (toolType == null) {
				throw new JsonSyntaxException("Unknown tool type: " + toolName);
			}

			return new ItemToolCondition(source, toolType);
		}

		@Override
		public ItemToolCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			ToolType toolType = ToolType.byName(buffer.readUtf());

			return new ItemToolCondition(source, toolType);
		}

	}

}
