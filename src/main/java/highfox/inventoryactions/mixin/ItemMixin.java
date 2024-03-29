package highfox.inventoryactions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.data.ActionsManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Item.class, priority = 800)
public class ItemMixin {

	@Inject(method = "overrideStackedOnOther(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
	private void overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> callback) {
		if (!callback.getReturnValueZ() && clickAction == ClickAction.SECONDARY) {
			IActionContext context = new ActionContext(slot.getItem(), stack, slot, player);
			ActionsManager.getActionForContext(context).ifPresent(action -> {
				if (!player.getLevel().isClientSide()) {
					action.runAction(context);
				}
				callback.setReturnValue(true);
			});
		}
	}

}
