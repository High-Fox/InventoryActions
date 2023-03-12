package highfox.inventoryactions.network.message;

import java.util.Map;

import highfox.inventoryactions.InventoryActionsClient;
import highfox.inventoryactions.action.InventoryAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class SyncActionsMessage implements IMessage {
	private final Map<ResourceLocation, InventoryAction> actions;

	public SyncActionsMessage(Map<ResourceLocation, InventoryAction> actions) {
		this.actions = actions;
	}

	public SyncActionsMessage(FriendlyByteBuf buffer) {
		Map<ResourceLocation, InventoryAction> map = buffer.readMap(FriendlyByteBuf::readResourceLocation, InventoryAction::fromNetwork);
		this.actions = map;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeMap(this.actions, FriendlyByteBuf::writeResourceLocation, (buf, action) -> {
			action.toNetwork(buf);
		});
	}

	@Override
	public void handle(Context context) {
		context.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InventoryActionsClient.syncActions(this));
		});

		context.setPacketHandled(true);
	}

	public Map<ResourceLocation, InventoryAction> getActions() {
		return this.actions;
	}

}
