package highfox.inventoryactions;

import net.minecraftforge.common.ForgeConfigSpec;

public class ActionConfig {
	public static final ForgeConfigSpec GENERAL_SPEC;
	public static ForgeConfigSpec.BooleanValue displayIconForValidActions;

	static {
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setupConfig(configBuilder);
		GENERAL_SPEC = configBuilder.build();
	}

	private static void setupConfig(ForgeConfigSpec.Builder builder) {
		displayIconForValidActions = builder
				.comment("Display a small marker icon on items that the picked up item can be used on. Default: true")
				.define("display_valid_action_icon", true);
	}

	protected static void save() {
		GENERAL_SPEC.save();
	}

}
