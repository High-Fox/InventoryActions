package com.highfox.inventoryactions.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ActionContext {
	private ItemStack target;
	private ItemStack using;
	private final Slot targetSlot;
	private final Player player;

	public ActionContext(ItemStack target, ItemStack using, Slot targetSlot, Player player) {
		this.target = target;
		this.using = using;
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

	public void consumeTarget() {
		this.target.shrink(1);
		this.targetSlot.setChanged();
	}

	public void consumeUsing() {
		this.using.shrink(1);
	}

	public void replaceTarget(ItemStack stack) {
		this.targetSlot.set(stack);
	}



}
