package com.highfox.inventoryactions.action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.highfox.inventoryactions.ActionConfig;
import com.highfox.inventoryactions.action.predicate.ActionPredicates;
import com.highfox.inventoryactions.registries.SoundRegistry;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

public class ActionHandler {
	// Gotta come up with something better than this list, might make the mod data driven eventually
	private static final InventoryAction LOG_STRIPPING_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> AxeItem.getAxeStrippingState(getBlockFromItem(s).defaultBlockState()) != null)
			.checkUsing(s -> s.canPerformAction(ToolActions.AXE_STRIP))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryStripping).and(ActionPredicates.ADVENTURE_MODE_CHECK))
			.runAction(ActionHandler::stripLog)
			.build();
	private static final InventoryAction POWDER_SOLIDIFYING_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> getBlockFromItem(s) instanceof ConcretePowderBlock)
			.checkUsing(s -> s.is(Items.WATER_BUCKET))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventorySolidifying).and(ActionPredicates.ADVENTURE_MODE_CHECK))
			.runAction(ActionHandler::solidifyPowder)
			.build();
	private static final InventoryAction PUMPKIN_CARVING_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> s.is(Items.PUMPKIN))
			.checkUsing(s -> s.canPerformAction(ToolActions.SHEARS_CARVE))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryCarving).and(ActionPredicates.ADVENTURE_MODE_CHECK))
			.runAction(ActionHandler::carvePumpkin)
			.build();
	private static final InventoryAction COPPER_REMOVE_WAX_OR_SCRAPE_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> WeatheringCopper.getPrevious(getBlockFromItem(s)).isPresent() || HoneycombItem.WAX_OFF_BY_BLOCK.get().containsKey(getBlockFromItem(s)))
			.checkUsing(s -> s.canPerformAction(ToolActions.AXE_SCRAPE) || s.canPerformAction(ToolActions.AXE_WAX_OFF))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryWaxingAndScraping).and(ActionPredicates.ADVENTURE_MODE_CHECK))
			.runAction(ActionHandler::scrapeOrRemoveWax)
			.build();
	private static final InventoryAction TILL_ROOTED_OR_COARSE_DIRT_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> s.is(Items.ROOTED_DIRT) || s.is(Items.COARSE_DIRT))
			.checkUsing(s -> s.canPerformAction(ToolActions.HOE_DIG))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryTilling).and(ActionPredicates.ADVENTURE_MODE_CHECK))
			.runAction(ActionHandler::tillRootedOrCoarseDirt)
			.build();
	private static final InventoryAction FILL_BOTTLES_FROM_BUCKET_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> s.is(Items.WATER_BUCKET))
			.checkUsing(s -> s.is(Items.GLASS_BOTTLE))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryBottleFilling))
			.runAction(ActionHandler::fillBottleFromBucket)
			.build();
	public static Map<Item, Item> MINECART_BY_CONTENTS = Maps.newHashMap(ImmutableMap.of(Items.CHEST, Items.CHEST_MINECART, Items.FURNACE, Items.FURNACE_MINECART, Items.HOPPER, Items.HOPPER_MINECART, Items.COMMAND_BLOCK, Items.COMMAND_BLOCK_MINECART, Items.TNT, Items.TNT_MINECART));
	private static final InventoryAction INSERT_INTO_MINECART_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> s.is(Items.MINECART))
			.checkUsing(s -> MINECART_BY_CONTENTS.containsKey(s.getItem()))
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryMinecartInsertion))
			.runAction(ActionHandler::insertBlockInMinecart)
			.build();
	private static final InventoryAction DYE_LEATHER_ARMOR_ACTION = new InventoryAction.Builder()
			.checkTarget(s -> s.getItem() instanceof DyeableLeatherItem)
			.checkUsing(s -> s.getItem() instanceof DyeItem)
			.masterCheck(ActionPredicates.configCheck(ActionConfig.enableInventoryLeatherArmorDyeing))
			.runAction(ActionHandler::dyeLeatherItem)
			.build();
	public static Set<InventoryAction> ACTIONS = Sets.newHashSet(LOG_STRIPPING_ACTION, POWDER_SOLIDIFYING_ACTION, PUMPKIN_CARVING_ACTION, COPPER_REMOVE_WAX_OR_SCRAPE_ACTION, TILL_ROOTED_OR_COARSE_DIRT_ACTION, FILL_BOTTLES_FROM_BUCKET_ACTION, INSERT_INTO_MINECART_ACTION, DYE_LEATHER_ARMOR_ACTION);

	public static boolean canPerformAnyAction(ItemStack targetStack, ItemStack usingStack, Slot slot, Player player) {
		ActionContext context = new ActionContext(targetStack, usingStack, slot, player);
		return ACTIONS.stream().anyMatch(action -> action.canPerformAction(context));
	}

	public static boolean runInventoryAction(ItemStack usingStack, Slot slot, ClickAction clickAction, Player player) {
		if (clickAction == ClickAction.SECONDARY) {
			ItemStack targetStack = slot.getItem();
			ActionContext context = new ActionContext(targetStack, usingStack, slot, player);
			Collection<ItemStack> results = Sets.newHashSet();

			ACTIONS.stream()
				.filter(action -> action.canPerformAction(context))
				.findFirst()
				.ifPresent(action -> action.performAction(context, results));

			if (results != null && !results.isEmpty()) {
				results.forEach(result -> {
					if (!player.addItem(result)) {
						player.drop(result, false);
					}
				});
			}

			if (!usingStack.isEmpty() && usingStack.isDamageableItem() && !player.getAbilities().instabuild) {
				usingStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			}

			return true;
		}

		return false;
	}

	// These assume any checks for invalid items have already been done, so check beforehand
	private static void stripLog(ActionContext context, Collection<ItemStack> results) {
		BlockState logState = getBlockFromItem(context.getTarget()).defaultBlockState();
		BlockState strippedState = AxeItem.getAxeStrippingState(logState);
		results.add(new ItemStack(strippedState.getBlock().asItem()));
		context.consumeTarget();
		playActionSound(context.getPlayer(), SoundEvents.AXE_STRIP, SoundSource.BLOCKS);
	}

	private static void solidifyPowder(ActionContext context, Collection<ItemStack> results) {
		Block powderBlock = getBlockFromItem(context.getTarget());
		BlockState solidifiedState = ((ConcretePowderBlock)powderBlock).concrete;
		results.add(new ItemStack(solidifiedState.getBlock().asItem()));
		context.consumeTarget();
		playActionSound(context.getPlayer(), SoundRegistry.solidify_concrete.get(), SoundSource.BLOCKS);
	}

	private static void carvePumpkin(ActionContext context, Collection<ItemStack> results) {
		results.add(new ItemStack(Items.CARVED_PUMPKIN));
		results.add(new ItemStack(Items.PUMPKIN_SEEDS, 4));
		context.consumeTarget();
		playActionSound(context.getPlayer(), SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS);
	}

	private static void scrapeOrRemoveWax(ActionContext context, Collection<ItemStack> results) {
		Block copperBlock = getBlockFromItem(context.getTarget());
		Optional<Block> waxRemovedBlock = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(copperBlock));
		Optional<Block> scrapedBlock = WeatheringCopper.getPrevious(copperBlock);
		if (waxRemovedBlock.isPresent()) {
			results.add(new ItemStack(waxRemovedBlock.get()));
			playActionSound(context.getPlayer(), SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS);
		} else if (scrapedBlock.isPresent()) {
			results.add(new ItemStack(scrapedBlock.get()));
			playActionSound(context.getPlayer(), SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS);
		}
		context.consumeTarget();
	}

	private static void tillRootedOrCoarseDirt(ActionContext context, Collection<ItemStack> results) {
		results.add(new ItemStack(Items.DIRT));
		if (context.getTarget().is(Items.ROOTED_DIRT)) {
			results.add(new ItemStack(Items.HANGING_ROOTS));
		}
		context.consumeTarget();
		playActionSound(context.getPlayer(), SoundEvents.HOE_TILL, SoundSource.BLOCKS);
	}

	private static void fillBottleFromBucket(ActionContext context, Collection<ItemStack> results) {
		ItemStack filledStack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
		results.add(filledStack);
		context.consumeUsing();
		playActionSound(context.getPlayer(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL);
	}

	private static void insertBlockInMinecart(ActionContext context, Collection<ItemStack> results) {
		ItemStack insertStack = context.getUsing();
		ItemStack resultMinecart = new ItemStack(MINECART_BY_CONTENTS.get(insertStack.getItem()));
		context.replaceTarget(resultMinecart);
		context.consumeUsing();
		playActionSound(context.getPlayer(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.NEUTRAL);
	}

	private static void dyeLeatherItem(ActionContext context, Collection<ItemStack> results) {
		List<DyeItem> dyeList = Lists.newArrayList((DyeItem)context.getUsing().getItem());
		ItemStack dyedLeatherItem = DyeableLeatherItem.dyeArmor(context.getTarget(), dyeList);
		context.replaceTarget(dyedLeatherItem);
		context.consumeUsing();
	}

	private static Block getBlockFromItem(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
			return Blocks.AIR;
		}

		return ((BlockItem)stack.getItem()).getBlock();
	}

	private static void playActionSound(Player player, SoundEvent sound, SoundSource source) {
		player.level.playSound(player, player.blockPosition(), sound, source, 1.0F, 1.0F);
	}

}
