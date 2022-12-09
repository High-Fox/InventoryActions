package highfox.inventoryactions.action.condition;

import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class ItemToolCondition extends ItemSourcingCondition {
	public static final Codec<ItemToolCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			UtilCodecs.enumCodec(ToolType::values, ToolType::byName, ToolType::getName).fieldOf("tool").forGetter(o -> o.toolType)
	).apply(instance, ItemToolCondition::new));

	private final ToolType toolType;

	public ItemToolCondition(ItemSource source, ToolType toolType) {
		super(source);
		this.toolType = toolType;
	}

	@Override
	public boolean test(ActionContext context) {
		ItemStack stack = this.source.get(context);

		return this.toolType.matches(stack);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.TOOL.get();
	}

	private static enum ToolType {
		SWORD("sword", ToolActions.DEFAULT_SWORD_ACTIONS),
		SHIELD("shield", ToolActions.DEFAULT_SHIELD_ACTIONS),
		PICKAXE("pickaxe", ToolActions.DEFAULT_PICKAXE_ACTIONS),
		AXE("axe", ToolActions.DEFAULT_AXE_ACTIONS),
		HOE("hoe", ToolActions.DEFAULT_HOE_ACTIONS),
		SHOVEL("shovel", ToolActions.DEFAULT_SHOVEL_ACTIONS),
		SHEARS("shears", ToolActions.DEFAULT_SHEARS_ACTIONS);

		protected static final ToolType[] VALUES = values();
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

}
