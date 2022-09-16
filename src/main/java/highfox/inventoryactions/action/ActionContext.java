package highfox.inventoryactions.action;

import highfox.inventoryactions.action.function.provider.IRequiresLootContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ActionContext {
	private final RandomSource random = RandomSource.createNewThreadLocalInstance();
	private final ItemStack target;
	private final ItemStack using;
	private final Slot targetSlot;
	private final Player player;

	public ActionContext(ItemStack target, ItemStack using, Slot targetSlot, Player player) {
		this.target = target.copy();
		this.using = using.copy();
		this.targetSlot = targetSlot;
		this.player = player;
	}

	public ItemStack getTarget() {
		return this.target;
	}

	public ItemStack getUsing() {
		return this.using;
	}

	public Slot getSlot() {
		return this.targetSlot;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Level getLevel() {
		return this.getPlayer().getLevel();
	}

	public RandomSource getRandom() {
		return this.random;
	}

	public LootContext getLootContext() {
		return this.getLootContext(ItemStack.EMPTY, null);
	}

	public LootContext getLootContext(ItemStack tool, BlockState blockState) {
		LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.getLevel())
				.withParameter(LootContextParams.THIS_ENTITY, this.getPlayer())
				.withParameter(LootContextParams.ORIGIN, this.player.position())
				.withLuck(this.player.getLuck())
				.withRandom(this.getRandom());

		if (!tool.isEmpty()) {
			builder = builder.withOptionalParameter(LootContextParams.TOOL, tool);
		}
		builder = builder.withOptionalParameter(LootContextParams.BLOCK_STATE, blockState);

		return builder.create(IRequiresLootContext.ACTION_PARAM_SET);
	}
}
