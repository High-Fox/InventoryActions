package highfox.inventoryactions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import highfox.inventoryactions.api.util.ActionsConstants;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.common.ForgeConfigSpec;

public class ActionConfig {
	protected static final Supplier<ConfigScreenFactory> CONFIG_SCREEN_SUPPLIER = () -> new ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen));
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

	private static void save() {
		GENERAL_SPEC.save();
	}

	public static final class ConfigScreen extends Screen {
		private static final Component TITLE = Component.translatable(ActionsConstants.MODID + ".configScreen.title");
		private static final String ENABLE_ACTION_ICONS = ActionsConstants.MODID + ".configScreen.enableActionIcons";
		private static final Component ENABLE_ACTION_ICONS_TOOLTIP = Component.translatable(ActionsConstants.MODID + ".configScreen.enableActionIcons.tooltip");
		private final Screen lastScreen;
		private OptionsList optionsList;

		private ConfigScreen(Screen lastScreen) {
			super(TITLE);
			this.lastScreen = lastScreen;
		}

		@Override
		protected void init() {
			super.init();

			this.optionsList = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);

			OptionInstance<Boolean> enableActionIcons = OptionInstance.createBoolean(ENABLE_ACTION_ICONS, OptionInstance.cachedConstantTooltip(ENABLE_ACTION_ICONS_TOOLTIP), ActionConfig.displayIconForValidActions.get(), updatedValue -> {
				ActionConfig.displayIconForValidActions.set(updatedValue);
				ActionConfig.save();
			});

			this.optionsList.addBig(enableActionIcons);

			this.addWidget(this.optionsList);
			this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
				this.minecraft.setScreen(this.lastScreen);
			}));
		}

		@Override
		public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
			this.renderBackground(matrix);
			this.optionsList.render(matrix, mouseX, mouseY, partialTicks);
			GuiComponent.drawCenteredString(matrix, this.font, TITLE.getVisualOrderText(), this.width / 2, 13, 0xFFFFFF);
			super.render(matrix, mouseX, mouseY, partialTicks);
			Optional<AbstractWidget> optional = this.optionsList.getMouseOver(mouseX, mouseY);
			List<FormattedCharSequence> list = optional.isPresent() && optional.get() instanceof TooltipAccessor ? ((TooltipAccessor)optional.get()).getTooltip() : ImmutableList.of();
			this.renderTooltip(matrix, list, mouseX, mouseY);
		}

	}

}
