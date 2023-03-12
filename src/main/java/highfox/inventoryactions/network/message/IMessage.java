package highfox.inventoryactions.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IMessage {
	void write(FriendlyByteBuf buffer);
	void handle(NetworkEvent.Context context);
}
