package com.highfox.inventoryactions.action.predicate;

import java.util.function.Predicate;

import com.highfox.inventoryactions.ActionConfig;
import com.highfox.inventoryactions.action.ActionContext;

import net.minecraftforge.common.ForgeConfigSpec;

public class ActionPredicates {
	public static final Predicate<ActionContext> ADVENTURE_MODE_CHECK = context -> context.getPlayer().getAbilities().mayBuild || ActionConfig.allowBlockActionsInAdventureMode.get();

	public static Predicate<ActionContext> configCheck(ForgeConfigSpec.BooleanValue configValue) {
		return context -> configValue.get();
	}

}
