package com.highfox.inventoryactions;

import java.util.List;

import com.highfox.inventoryactions.action.ActionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = InventoryActions.MODID)
public class ClientHooks {

	@SubscribeEvent
	public static void drawGuiBackground(ContainerScreenEvent.Render.Background event) {
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		Minecraft minecraft = Minecraft.getInstance();
		List<Slot> slots = screen.getMenu().slots;
		ItemStack carriedStack = screen.getMenu().getCarried();

		if (ActionConfig.displayIconForValidActions.get() && minecraft.player != null && (!(screen instanceof CreativeModeInventoryScreen) || ((CreativeModeInventoryScreen)screen).getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId())) {
			for (int i = 0; i < slots.size(); i++) {
				Slot slot = slots.get(i);
				ItemStack targetStack = slot.getItem();

				if (!targetStack.isEmpty() && !carriedStack.isEmpty() && ActionHandler.canPerformAnyAction(targetStack, carriedStack, slot, minecraft.player)) {
					int x = slot.x;
					int y = slot.y;
					float textScale = 0.65F;
					FormattedCharSequence s = FormattedCharSequence.forward("+", Style.EMPTY.withColor(ChatFormatting.WHITE));

					PoseStack stack = event.getPoseStack();
					RenderSystem.enableDepthTest();
					stack.pushPose();
					stack.translate((double)screen.getGuiLeft(), (double)screen.getGuiTop(), 0.0D);
					stack.scale(textScale, textScale, 1.0F);
					stack.translate(0.0D, 0.0D, (double)300.0F);
					MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					minecraft.font.drawInBatch(s, (float)(x / textScale) + 18, (float)(y / textScale), 16777215, true, stack.last().pose(), bufferSource, false, 0, 15728880);
					bufferSource.endBatch();
					RenderSystem.disableDepthTest();
					stack.popPose();
				}
			}
		}
	}

}
