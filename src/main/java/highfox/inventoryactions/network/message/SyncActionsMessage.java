package highfox.inventoryactions.network.message;

import java.util.Map;

import com.mojang.serialization.DynamicOps;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.util.ClientMethods;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class SyncActionsMessage implements IMessage {
	private Map<ResourceLocation, InventoryAction> actions;

	public SyncActionsMessage(Map<ResourceLocation, InventoryAction> actions) {
		this.actions = actions;
	}

	public SyncActionsMessage(FriendlyByteBuf buf) {
		DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.BUILTIN.get());
		Map<ResourceLocation, InventoryAction> map = buf.readMap(FriendlyByteBuf::readResourceLocation, buffer -> {
			return this.deserializeAction(buffer, ops);
		});
		this.actions = map;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.BUILTIN.get());
		buf.writeMap(this.actions, FriendlyByteBuf::writeResourceLocation, (buffer, action) -> {
			this.serializeAction(buffer, action, ops);
		});
	}

	@Override
	public void handle(Context context) {
		context.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMethods.syncActions(this));
		});

		context.setPacketHandled(true);
	}

	public void serializeAction(FriendlyByteBuf buf, InventoryAction action, DynamicOps<Tag> ops) {
		InventoryAction.NETWORK_CODEC.encodeStart(ops, action).resultOrPartial(InventoryActions.LOG::error).ifPresent(tag -> {
			buf.writeNbt((CompoundTag)tag);
		});
	}

	public InventoryAction deserializeAction(FriendlyByteBuf buf, DynamicOps<Tag> ops) {
		InventoryAction action = InventoryAction.NETWORK_CODEC.parse(ops, buf.readAnySizeNbt()).resultOrPartial(InventoryActions.LOG::error).orElseThrow();

		return action;
	}

	public Map<ResourceLocation, InventoryAction> getActions() {
		return this.actions;
	}

}
