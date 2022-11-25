package highfox.inventoryactions.util;

import com.google.common.collect.ImmutableMap;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.data.ActionsManager;
import highfox.inventoryactions.network.message.SyncActionsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMethods {

	public static void syncActions(SyncActionsMessage msg) {
		ImmutableMap<ResourceLocation, InventoryAction> actions = ImmutableMap.copyOf(msg.getActions());
		ActionsManager.setActions(actions);
		InventoryActions.LOG.debug("Loaded {} inventory actions from the server", actions.size());
	}

	public static GameType getClientGameMode() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		PlayerInfo playerInfo = connection.getPlayerInfo(connection.getLocalGameProfile().getId());
		return playerInfo.getGameMode();
	}

}
