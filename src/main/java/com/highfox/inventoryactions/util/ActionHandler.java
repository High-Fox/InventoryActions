package com.highfox.inventoryactions.util;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.highfox.inventoryactions.ActionConfig;
import com.highfox.inventoryactions.registries.SoundRegistry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

public class ActionHandler {
	private static final InventoryAction STRIPPING_ACTION = new InventoryAction(s -> AxeItem.getAxeStrippingState(getBlockFromItem(s).defaultBlockState()) != null, s -> s.getItem() instanceof AxeItem || s.canPerformAction(ToolActions.AXE_STRIP), () -> ActionConfig.enableInventoryStripping.get(), ActionHandler::stripStack);
	private static final InventoryAction SOLIDIFYING_ACTION = new InventoryAction(s -> getBlockFromItem(s) instanceof ConcretePowderBlock, s -> s.is(Items.WATER_BUCKET), () -> ActionConfig.enableInventorySolidifying.get(), ActionHandler::solidifyStack);
	private static final InventoryAction CARVING_ACTION = new InventoryAction(s -> getBlockFromItem(s) instanceof PumpkinBlock, s -> s.is(Items.SHEARS) || s.canPerformAction(ToolActions.SHEARS_CARVE), () -> ActionConfig.enableInventoryCarving.get(), ActionHandler::carvePumpkin);
	private static final Set<InventoryAction> BLOCK_ACTIONS = ImmutableSet.of(STRIPPING_ACTION, SOLIDIFYING_ACTION, CARVING_ACTION);

	public static boolean canPerformAnyAction(ItemStack targetStack, ItemStack usingStack, Slot slot, Player player) {
		return BLOCK_ACTIONS.stream().anyMatch(action -> action.canPerformAction(targetStack, usingStack, slot, player));
	}

	public static boolean doInventoryAction(ItemStack usingStack, Slot slot, ClickAction clickAction, Player player) {
		if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
			ItemStack targetStack = slot.safeTake(1, Integer.MAX_VALUE, player);
			Collection<ItemStack> results = Sets.newHashSet();

			if (player.getAbilities().mayBuild || ActionConfig.allowBlockActionsInAdventureMode.get()) {
				BLOCK_ACTIONS.stream()
					.filter(action -> action.canPerformAction(targetStack, usingStack, slot, player))
					.findFirst()
					.ifPresent(action -> action.performAction(targetStack, usingStack, player, results));
			}

			if (results != null && !results.isEmpty()) {
				results.forEach(stack -> {
					if (!player.addItem(stack)) {
						player.drop(stack, false);
					}
				});
			}

			if (usingStack.isDamageableItem()) {
				usingStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			}

			return true;
		}

		return false;
	}

	// These assume any checks for invalid items have already been done, so check beforehand
	private static void stripStack(ItemStack targetStack, ItemStack usingStack, Player player, Collection<ItemStack> results) {
		BlockState originalState = getBlockFromItem(targetStack).defaultBlockState();
		BlockState strippedState = AxeItem.getAxeStrippingState(originalState);
		results.add(new ItemStack(strippedState.getBlock().asItem()));
		player.level.playSound(player, player.blockPosition(), SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private static void solidifyStack(ItemStack targetStack, ItemStack usingStack, Player player, Collection<ItemStack> results) {
		Block originalBlock = getBlockFromItem(targetStack);
		BlockState solidifiedState = ((ConcretePowderBlock)originalBlock).concrete;
		results.add(new ItemStack(solidifiedState.getBlock().asItem()));
		player.level.playSound(player, player.blockPosition(), SoundRegistry.solidify_concrete.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private static void carvePumpkin(ItemStack targetStack, ItemStack usingStack, Player player, Collection<ItemStack> results) {
		results.add(new ItemStack(Items.CARVED_PUMPKIN));
		results.add(new ItemStack(Items.PUMPKIN_SEEDS, 4));
		player.level.playSound(player, player.blockPosition(), SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private static Block getBlockFromItem(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
			return Blocks.AIR;
		}

		return ((BlockItem)stack.getItem()).getBlock();
	}

	public static class InventoryAction {
		private Predicate<ItemStack> canPerformActionOn;
		private Predicate<ItemStack> canPerformActionWith;
		private Supplier<Boolean> isEnabledSupplier;
		private QuadConsumer<ItemStack, ItemStack, Player, Collection<ItemStack>> runAction;

		public InventoryAction(Predicate<ItemStack> canPerformActionOn, Predicate<ItemStack> canPerformActionWith, Supplier<Boolean> isEnabledSupplier, QuadConsumer<ItemStack, ItemStack, Player, Collection<ItemStack>> runAction) {
			this.canPerformActionOn = canPerformActionOn;
			this.canPerformActionWith = canPerformActionWith;
			this.runAction = runAction;
			this.isEnabledSupplier = isEnabledSupplier;
		}

		public boolean canPerformAction(ItemStack targetStack, ItemStack usingStack, Slot slot, Player player) {
			return this.isEnabledSupplier.get()
					&& slot.allowModification(player)
					&& (player.getAbilities().mayBuild || ActionConfig.allowBlockActionsInAdventureMode.get())
					&& !targetStack.isEmpty()
					&& !usingStack.isEmpty()
					&& this.canPerformActionOn.test(targetStack)
					&& this.canPerformActionWith.test(usingStack);
		}

		public void performAction(ItemStack targetStack, ItemStack usingStack, Player player, Collection<ItemStack> results) {
			this.runAction.consume(targetStack, usingStack, player, results);
		}
	}

}
