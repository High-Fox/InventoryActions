package highfox.inventoryactions.action.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;

public class InvertCondition implements IActionCondition {
	public static final Codec<InvertCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IActionCondition.CODEC.fieldOf("condition").forGetter(o -> o.condition)
	).apply(instance, InvertCondition::new));

	private final IActionCondition condition;

	public InvertCondition(IActionCondition condition) {
		this.condition = condition;
	}

	@Override
	public boolean test(ActionContext context) {
		return !this.condition.test(context);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.INVERT.get();
	}

}
