package highfox.inventoryactions.action;

import javax.annotation.Nullable;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.mixin.LootContextParamSetsInvoker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ActionContext implements IActionContext {
	private final ItemStack target;
	private final ItemStack using;
	private final Slot targetSlot;
	private final Player player;
	@Nullable
	private RandomSource random;
	public static final LootContextParamSet ACTION_PARAM_SET = LootContextParamSetsInvoker.invokeRegister(ActionsConstants.MODID + ":inventory_action", builder -> {
		builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).optional(LootContextParams.TOOL).optional(LootContextParams.BLOCK_STATE);
	});

	public ActionContext(ItemStack target, ItemStack using, Slot targetSlot, Player player) {
		this.target = target.copy();
		this.using = using.copy();
		this.targetSlot = targetSlot;
		this.player = player;
	}

	@Override
	public ItemStack getTarget() {
		return this.target;
	}

	@Override
	public ItemStack getUsing() {
		return this.using;
	}

	@Override
	public Slot getSlot() {
		return this.targetSlot;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public Level getLevel() {
		return this.getPlayer().getLevel();
	}

	@Override
	public RandomSource getRandom() {
		if (this.random == null) {
			this.random = RandomSource.createNewThreadLocalInstance();
		}

		return this.random;
	}

	@Override
	public LootContext getLootContext(ItemStack tool, BlockState blockState) {
		if (this.getLevel().isClientSide()) {
			throw new IllegalStateException("Attempted to create loot context on the client");
		}

		LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.getLevel())
				.withParameter(LootContextParams.THIS_ENTITY, this.player)
				.withParameter(LootContextParams.ORIGIN, this.player.position())
				.withOptionalParameter(LootContextParams.BLOCK_STATE, blockState)
				.withLuck(this.player.getLuck())
				.withRandom(this.getRandom());

		if (!tool.isEmpty()) {
			builder = builder.withOptionalParameter(LootContextParams.TOOL, tool);
		}

		return builder.create(ACTION_PARAM_SET);
	}
}
