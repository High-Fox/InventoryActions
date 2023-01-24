package highfox.inventoryactions.api.itemsource;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

import highfox.inventoryactions.api.action.IActionContext;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * An item source that accesses a container slot
 */
public class SlotSource implements IItemSource {
	private final int slot;
	private final Function<IActionContext, IntFunction<SlotAccess>> slotGetter;
	private final Consumer<IActionContext> onUpdate;

	/**
	 * Constructs a new slot source
	 *
	 * @param slot the index of the slot
	 * @param slotGetter a function that returns another function which takes in an integer and returns a {@link SlotAccess}
	 * @param onUpdate a consumer that broadcasts changes
	 */
	public SlotSource(int slot, Function<IActionContext, IntFunction<SlotAccess>> slotGetter, Consumer<IActionContext> onUpdate) {
		this.slot = slot;
		this.slotGetter = slotGetter;
		this.onUpdate = onUpdate;
	}

	/**
	 * Creates a source from a slot in the player's inventory
	 *
	 * @param slot the index of the slot
	 * @return a slot source for the player's inventory
	 */
	public static SlotSource player(int slot) {
		return new SlotSource(slot, context -> context.getPlayer()::getSlot, context -> {
			Player player = context.getPlayer();
			player.getInventory().setChanged();
			player.inventoryMenu.broadcastChanges();
		});
	}

	@Override
	public ItemStack get(IActionContext context) {
		return this.getSlotAccess(context).get();
	}

	@Override
	public void setAndUpdate(IActionContext context, ItemStack stack) {
		SlotAccess access = this.getSlotAccess(context);
		access.set(stack);
		this.onUpdate.accept(context);
	}

	protected SlotAccess getSlotAccess(IActionContext context) {
		return this.slotGetter.apply(context).apply(this.slot);
	}
}