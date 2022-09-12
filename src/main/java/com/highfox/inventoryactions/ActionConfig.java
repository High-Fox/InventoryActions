package com.highfox.inventoryactions;

import net.minecraftforge.common.ForgeConfigSpec;

public class ActionConfig {
	public static final ForgeConfigSpec GENERAL_SPEC;
	public static ForgeConfigSpec.BooleanValue displayIconForValidActions;
	public static ForgeConfigSpec.BooleanValue allowBlockActionsInAdventureMode;
	public static ForgeConfigSpec.BooleanValue enableInventoryStripping;
	public static ForgeConfigSpec.BooleanValue enableInventorySolidifying;
	public static ForgeConfigSpec.BooleanValue enableInventoryCarving;

	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();
	}

	private static void setupConfig(ForgeConfigSpec.Builder builder) {
		displayIconForValidActions = builder
				.comment("Display a small marker icon on items that the picked up item can be used on. Default: true")
				.define("display_valid_action_icon", true);
		builder.push("Block Actions");
		allowBlockActionsInAdventureMode = builder
				.comment("Allow block actions within the inventory when in adventure mode. Default: false")
				.define("allow_block_actions_in_adventure_mode", false);
		enableInventoryStripping = builder
				.comment("Enable the log stripping action. Default: true")
				.define("enable_inventory_log_stripping", true);
		enableInventorySolidifying = builder
				.comment("Enable the concrete solidifying action. Default: true")
				.define("enable_inventory_concrete_solidifying", true);
		enableInventoryCarving = builder
				.comment("Enable the pumpkin carving action. Default: true")
				.define("enable_inventory_pumpkin_carving", true);
		builder.pop();
	}

}
