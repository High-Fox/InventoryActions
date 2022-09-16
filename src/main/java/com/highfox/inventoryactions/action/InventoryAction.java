package com.highfox.inventoryactions.action;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryAction {
	private final Predicate<ItemStack> canPerformActionOn;
	private final Predicate<ItemStack> canPerformActionWith;
	private final Predicate<ActionContext> masterCheck;
	private final BiConsumer<ActionContext, Collection<ItemStack>> runAction;

	public InventoryAction(Predicate<ItemStack> canPerformActionOn, Predicate<ItemStack> canPerformActionWith, Predicate<ActionContext> masterCheck, BiConsumer<ActionContext, Collection<ItemStack>> runAction) {
		this.canPerformActionOn = canPerformActionOn;
		this.canPerformActionWith = canPerformActionWith;
		this.masterCheck = masterCheck;
		this.runAction = runAction;
	}

	public boolean canPerformAction(ActionContext context) {
		ItemStack targetStack = context.getTarget();
		ItemStack usingStack = context.getUsing();
		Player player = context.getPlayer();

		return this.masterCheck.test(context)
				&& player != null
				&& context.getSlot().allowModification(player)
				&& !targetStack.isEmpty()
				&& !usingStack.isEmpty()
				&& this.canPerformActionOn.test(targetStack)
				&& this.canPerformActionWith.test(usingStack);
	}

	public void performAction(ActionContext context, Collection<ItemStack> results) {
		this.runAction.accept(context, results);
	}

	public static class Builder {
		private Predicate<ItemStack> targetCheck;
		private Predicate<ItemStack> usingCheck;
		private Predicate<ActionContext> masterCheck;
		private BiConsumer<ActionContext, Collection<ItemStack>> runAction;

		public InventoryAction.Builder checkTarget(Predicate<ItemStack> targetPredicate) {
			this.targetCheck = targetPredicate;
			return this;
		}

		public InventoryAction.Builder checkUsing(Predicate<ItemStack> initiatorPredicate) {
			this.usingCheck = initiatorPredicate;
			return this;
		}

		public InventoryAction.Builder masterCheck(Predicate<ActionContext> masterCheck) {
			this.masterCheck = masterCheck;
			return this;
		}

		public InventoryAction.Builder runAction(BiConsumer<ActionContext, Collection<ItemStack>> runAction) {
			this.runAction = runAction;
			return this;
		}

		public InventoryAction build() {
			return new InventoryAction(this.targetCheck, this.usingCheck, this.masterCheck, this.runAction);
		}

	}

}