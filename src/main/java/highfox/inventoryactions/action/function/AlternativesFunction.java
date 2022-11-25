package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.condition.IActionCondition;

public class AlternativesFunction implements IActionFunction {
	public static final Codec<AlternativesFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.pair(IActionCondition.CODEC.listOf().fieldOf("conditions").codec(), IActionFunction.CODEC.listOf().fieldOf("functions").codec()).listOf().fieldOf("entries").forGetter(o -> o.entries)
	).apply(instance, AlternativesFunction::new));

	private final List<Pair<List<IActionCondition>, List<IActionFunction>>> entries;

	public AlternativesFunction(List<Pair<List<IActionCondition>, List<IActionFunction>>> entries) {
		this.entries = entries;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {

		for (Pair<List<IActionCondition>, List<IActionFunction>> pair : this.entries) {
			if (pair.getFirst().stream().allMatch(condition -> condition.test(context))) {
				pair.getSecond().forEach(function -> function.run(workQueue, context));
				break;
			}
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.ALTERNATIVES.get();
	}

}
