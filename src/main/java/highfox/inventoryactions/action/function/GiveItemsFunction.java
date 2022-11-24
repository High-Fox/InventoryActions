package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.function.provider.IItemProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GiveItemsFunction implements IActionFunction {
	public static final Codec<GiveItemsFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IItemProvider.CODEC.listOf().fieldOf("items").forGetter(o -> o.providers)
	).apply(instance, GiveItemsFunction::new));

	private final List<IItemProvider> providers;

	public GiveItemsFunction(List<IItemProvider> providers) {
		this.providers = providers;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		ObjectArrayList<ItemStack> stacks = new ObjectArrayList<ItemStack>();

		if (!context.getLevel().isClientSide()) {
			this.providers.stream().forEach(provider -> {
				provider.addItems(context, context.getRandom(), stacks);
			});
		}

		workQueue.add(() -> {
			for (ItemStack stack : stacks) {
				giveItem(stack, context);
			}
		});
	}

	public static void giveItem(ItemStack stack, ActionContext context) {
		Player player = context.getPlayer();

		if (!stack.isStackable() && (!context.getSlot().hasItem() || context.getUsing().isEmpty())) {
			if (!context.getSlot().hasItem()) {
				context.getSlot().set(stack);
			} else {
				player.containerMenu.setCarried(stack);
			}
		} else if (!player.addItem(stack)) {
			if (player.containerMenu != player.inventoryMenu) {
				player.containerMenu.broadcastChanges();
			} else {
				player.inventoryMenu.broadcastChanges();
			}
			player.drop(stack, false);
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.GIVE_ITEMS.get();
	}
}
