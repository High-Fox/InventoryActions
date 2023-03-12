package highfox.inventoryactions;

import highfox.inventoryactions.action.condition.ActionConditionTypes;
import highfox.inventoryactions.action.function.ActionFunctionTypes;
import highfox.inventoryactions.action.function.provider.ItemProviderTypes;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.data.ActionsManager;
import highfox.inventoryactions.network.ActionsNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ActionsConstants.MODID)
public class InventoryActions {
	private static final DeferredRegister<SoundEvent> DEFERRED_SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ActionsConstants.MODID);
	public static final RegistryObject<SoundEvent> SOLIDIFY_CONCRETE = DEFERRED_SOUND_EVENTS.register("solidify_concrete", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ActionsConstants.MODID, "solidify_concrete")));

	public InventoryActions() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ActionConfig.GENERAL_SPEC, "inventory-actions.toml");
		MinecraftForge.EVENT_BUS.register(this);

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> InventoryActionsClient::init);
		}

		DEFERRED_SOUND_EVENTS.register(bus);
		ItemProviderTypes.DEFERRED_PROVIDER_SERIALIZERS.register(bus);
		ActionConditionTypes.DEFERRED_CONDITION_SERIALIZERS.register(bus);
		ActionFunctionTypes.DEFERRED_FUNCTION_SERIALIZERS.register(bus);
	}

	public void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(ActionsNetwork::init);
	}

	@SubscribeEvent
	public void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new ActionsManager());
	}

	@SubscribeEvent
	public void syncData(OnDatapackSyncEvent event) {
		if (event.getPlayer() != null) {
			ServerPlayer player = event.getPlayer();
			if (!player.connection.connection.isMemoryConnection()) {
				ActionsNetwork.sendToPlayer(player, ActionsManager.getSyncMessage());
			}
		} else {
			PlayerList playerList = event.getPlayerList();
			if (!playerList.getServer().isSingleplayer()) {
				ActionsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), ActionsManager.getSyncMessage());
			}
		}
	}

}
