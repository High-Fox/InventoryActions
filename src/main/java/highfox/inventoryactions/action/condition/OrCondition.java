package highfox.inventoryactions.action.condition;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;

public class OrCondition implements IActionCondition {
	public static final Codec<OrCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IActionCondition.CODEC.listOf().fieldOf("conditions").forGetter(o -> o.conditions)
	).apply(instance, OrCondition::new));

	private final List<IActionCondition> conditions;

	public OrCondition(List<IActionCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(ActionContext context) {
		return this.conditions.stream().anyMatch(condition -> condition.test(context));
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ANY.get();
	}

}
