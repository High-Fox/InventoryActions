package highfox.inventoryactions.action.condition;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.action.ActionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public interface IActionCondition {
	public static final Codec<IActionCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ActionConditionType.CONDITION_TYPES.get().getCodec()).dispatch(IActionCondition::getType, ActionConditionType::getCodec);

	boolean test(ActionContext context);
	ActionConditionType getType();

	default ResourceLocation getRegistryName() {
		return ActionConditionType.CONDITION_TYPES.get().getKey(this.getType());
	}
}
