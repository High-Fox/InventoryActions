package highfox.inventoryactions.api.action;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Contains any context surrounding an inventory action's conditions or functions being run
 */
public interface IActionContext {

	/**
	 * Gets the item was right clicked
	 *
	 * @return the clicked item
	 */
	ItemStack getTarget();

	/**
	 * Gets the item currently being carried within the inventory
	 *
	 * @return the carried item
	 */
	ItemStack getUsing();

	/**
	 * Gets the clicked slot under the cursor
	 *
	 * @return the clicked slot
	 */
	Slot getSlot();

	/**
	 * Gets the player this context is for
	 *
	 * @return the player
	 */
	Player getPlayer();

	/**
	 * Gets the level the player is currently in
	 *
	 * @return the player's level
	 */
	Level getLevel();

	/**
	 * Gets a threaded (thread-unsafe) {@link RandomSource}
	 *
	 * @return a threaded random source
	 */
	RandomSource getRandom();

	/**
	 * Creates a loot context with empty {@link LootContextParams#TOOL TOOL}
	 * and {@link LootContextParams#BLOCK_STATE BLOCK_STATE} params
	 * <p>This must only be called when on the <b>server</b>
	 *
	 * @return a loot context
	 * @throws IllegalStateException if called on the client
	 * @see {@link #getLootContext(ItemStack, BlockState)}
	 */
	default LootContext getLootContext() {
		return this.getLootContext(ItemStack.EMPTY, null);
	}

	/**
	 * Creates a loot context for use with a {@link LootContextUser LootContextUser}.
	 * <p>This must only be called when on the <b>server</b>
	 *
	 * @param tool the item to use as the {@link LootContextParams#TOOL TOOL} param
	 * @param blockState the block state to use as
	 * 		  the {@link LootContextParams#BLOCK_STATE BLOCK_STATE} param
	 * @return a loot context
	 * @throws IllegalStateException if called on the client
	 */
	LootContext getLootContext(ItemStack tool, BlockState blockState);

}