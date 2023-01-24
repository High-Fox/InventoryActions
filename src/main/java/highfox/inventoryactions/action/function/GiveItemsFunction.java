package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Queue;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.itemprovider.IItemProvider;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.util.SerializationUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GiveItemsFunction implements IActionFunction {
	private final List<IItemProvider> providers;

	public GiveItemsFunction(List<IItemProvider> providers) {
		this.providers = providers;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
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

			if (!stacks.isEmpty()) {
				Player player = context.getPlayer();
				if (player.hasContainerOpen()) {
					player.containerMenu.broadcastChanges();
				} else {
					player.inventoryMenu.broadcastChanges();
				}
			}
		});
	}

	public static void giveItem(ItemStack stack, IActionContext context) {
		Player player = context.getPlayer();

		if (!stack.isStackable() && (!context.getSlot().hasItem() || context.getUsing().isEmpty())) {
			if (!context.getSlot().hasItem()) {
				context.getSlot().set(stack);
			} else {
				player.containerMenu.setCarried(stack);
			}
		} else if (!player.addItem(stack)) {
			player.drop(stack, false);
		}
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.GIVE_ITEMS.get();
	}

	public static class Deserializer implements IDeserializer<GiveItemsFunction> {

		@Override
		public GiveItemsFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			List<IItemProvider> providers = context.deserialize(GsonHelper.getAsJsonArray(json, "items"), SerializationUtils.ITEM_PROVIDER_LIST_TYPE);

			return new GiveItemsFunction(providers);
		}

		@Override
		public GiveItemsFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}
}
