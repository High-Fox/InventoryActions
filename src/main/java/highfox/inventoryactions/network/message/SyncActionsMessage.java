package highfox.inventoryactions.network.message;

import java.util.Map;

import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.util.ClientMethods;
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

	public SyncActionsMessage(FriendlyByteBuf buf) {
		Map<ResourceLocation, InventoryAction> map = buf.readMap(FriendlyByteBuf::readResourceLocation, InventoryAction::fromNetwork);
		this.actions = map;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.actions, FriendlyByteBuf::writeResourceLocation, (buffer, action) -> {
			action.toNetwork(buffer);
		});
	}

	@Override
	public void handle(Context context) {
		context.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMethods.syncActions(this));
		});

		context.setPacketHandled(true);
	}

	public Map<ResourceLocation, InventoryAction> getActions() {
		return this.actions;
	}

}
