package highfox.inventoryactions.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IMessage {
	void write(FriendlyByteBuf buf);
	void handle(NetworkEvent.Context context);
}
