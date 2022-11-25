package highfox.inventoryactions.action;

import java.util.List;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.condition.IActionCondition;
import highfox.inventoryactions.action.function.IActionFunction;
import net.minecraft.world.entity.player.Player;;

public class InventoryAction {
	public static final Codec<InventoryAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IActionCondition.CODEC.listOf().fieldOf("conditions").forGetter(o -> o.conditions),
			IActionFunction.CODEC.listOf().fieldOf("functions").forGetter(o -> o.functions)
	).apply(instance, InventoryAction::new));
	// Clients only need to know the conditions
	public static final Codec<InventoryAction> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IActionCondition.CODEC.listOf().fieldOf("conditions").forGetter(o -> o.conditions)
	).apply(instance, conditions -> new InventoryAction(conditions, ImmutableList.of())));

	private final List<IActionCondition> conditions;
	private final List<IActionFunction> functions;
	private final Queue<Runnable> workQueue = Queues.newLinkedBlockingQueue();

	public InventoryAction(List<IActionCondition> conditions, List<IActionFunction> functions) {
		this.conditions = conditions;
		this.functions = functions;
	}

	public boolean canRunAction(ActionContext context) {
		Player player = context.getPlayer();

		return player != null
				&& !context.getTarget().isEmpty()
				&& !context.getUsing().isEmpty()
				&& context.getSlot().allowModification(player)
				&& this.conditions.stream().allMatch(condition -> {
					try {
						return condition.test(context);
					} catch (Exception e) {
						InventoryActions.LOG.error("Error checking condition {}: {}", condition.getRegistryName(), e.getMessage());
						return false;
					}
				});
	}

	public void runAction(ActionContext context) {
		if (!this.functions.isEmpty() && !context.getLevel().isClientSide()) {
			this.functions.stream().forEachOrdered(function -> {
				try {
					function.run(this.workQueue, context);
				} catch (Exception e) {
					InventoryActions.LOG.error("Error running function {}: {}", function.getRegistryName(), e.getMessage());
				}
			});

			while (!this.workQueue.isEmpty()) {
				try {
					this.workQueue.poll().run();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}