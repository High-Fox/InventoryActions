package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.condition.IActionCondition;

public class ConditionalFunction implements IActionFunction {
	public static final Codec<ConditionalFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IActionFunction.CODEC.fieldOf("function").forGetter(o -> o.function),
			IActionCondition.CODEC.listOf().fieldOf("conditions").forGetter(o -> o.conditions)
	).apply(instance, ConditionalFunction::new));

	private final IActionFunction function;
	private final List<IActionCondition> conditions;

	public ConditionalFunction(IActionFunction function, List<IActionCondition> conditions) {
		this.function = function;
		this.conditions = conditions;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		if (this.conditions.stream().allMatch(condition -> condition.test(context))) {
			this.function.run(workQueue, context);
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.CONDITIONAL.get();
	}

}
