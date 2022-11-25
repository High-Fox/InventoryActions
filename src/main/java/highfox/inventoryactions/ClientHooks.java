package highfox.inventoryactions;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.data.ActionsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = InventoryActions.MODID)
public class ClientHooks {
	private static final FormattedCharSequence ICON = FormattedCharSequence.forward("+", Style.EMPTY.withColor(ChatFormatting.WHITE));
	private static final float ICON_SCALE = 0.65F;

	@SubscribeEvent
	public void playerDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		if (event.getPlayer() != null) {
			ActionsManager.clearActions();
		}
	}

	@SubscribeEvent
	public static void drawGuiBackground(ContainerScreenEvent.DrawBackground event) {
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		Minecraft minecraft = Minecraft.getInstance();
		List<Slot> slots = screen.getMenu().slots;
		ItemStack carriedStack = screen.getMenu().getCarried();

		if (ActionConfig.displayIconForValidActions.get() && minecraft.player != null && !(screen instanceof CreativeModeInventoryScreen)) {
			PoseStack matrix = event.getPoseStack();
			RenderSystem.enableDepthTest();
			matrix.pushPose();
			matrix.translate(screen.getGuiLeft(), screen.getGuiTop(), 0.0D);
			matrix.scale(ICON_SCALE, ICON_SCALE, 1.0F);
			matrix.translate(0.0D, 0.0D, 300.0D);

			for (int i = 0; i < slots.size(); i++) {
				Slot slot = slots.get(i);
				ItemStack targetStack = slot.getItem();
				ActionContext context = new ActionContext(targetStack, carriedStack, slot, minecraft.player);

				if (!targetStack.isEmpty() && !carriedStack.isEmpty() && ActionsManager.canRunAny(context)) {
					int x = slot.x;
					int y = slot.y;

					MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					minecraft.font.drawInBatch(ICON, x / ICON_SCALE + 18, y / ICON_SCALE, 0xFFFFFF, true, matrix.last().pose(), bufferSource, false, 0, 15728880);
					bufferSource.endBatch();
				}
			}

			matrix.popPose();
			RenderSystem.disableDepthTest();
		}
	}

}
