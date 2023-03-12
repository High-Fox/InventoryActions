package highfox.inventoryactions.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.network.message.IMessage;
import highfox.inventoryactions.network.message.SyncActionsMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ActionsNetwork {
	private static final String VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(ActionsConstants.MODID, "action_syncing"),
			() -> "1",
			VERSION::equals,
			VERSION::equals
	);
	private static int index = 0;

	public static void init() {
		registerMessage(SyncActionsMessage.class, NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(ServerPlayer player, IMessage msg) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
	}

	private static <T extends IMessage> void registerMessage(Class<T> clazz, NetworkDirection direction) {
		BiConsumer<T, FriendlyByteBuf> encoder = T::write;

		Function<FriendlyByteBuf, T> decoder = buffer -> {
			try {
				T instance = clazz.getConstructor(FriendlyByteBuf.class).newInstance(buffer);
				return instance;
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Error creating new " + clazz.getSimpleName() + " instance: " + e.getMessage());
			}
		};

		BiConsumer<T, Supplier<NetworkEvent.Context>> handler = (msg, contextSupplier) -> {
			NetworkEvent.Context context = contextSupplier.get();

			if (context.getDirection() != direction) {
				return;
			}

			msg.handle(context);
		};

		CHANNEL.registerMessage(index, clazz, encoder, decoder, handler);
		index++;
	}

}
