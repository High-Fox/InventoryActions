package highfox.inventoryactions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import highfox.inventoryactions.action.condition.ActionConditionType;
import highfox.inventoryactions.action.function.ActionFunctionType;
import highfox.inventoryactions.action.function.provider.ItemProviderType;
import highfox.inventoryactions.data.ActionsManager;
import highfox.inventoryactions.network.ActionsNetwork;
import highfox.inventoryactions.network.message.SyncActionsMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(InventoryActions.MODID)
public class InventoryActions {
	public static final String MODID = "inventoryactions";
	public static final Logger LOG = LogManager.getLogger(MODID);
	private static final DeferredRegister<SoundEvent> DEFERRED_SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
	public static final RegistryObject<SoundEvent> SOLIDIFY_CONCRETE = DEFERRED_SOUND_EVENTS.register("solidify_concrete", () -> new SoundEvent(new ResourceLocation(MODID, "solidify_concrete")));

	public InventoryActions() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ActionConfig.GENERAL_SPEC, "inventory-actions.toml");
		MinecraftForge.EVENT_BUS.register(this);

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, ActionConfig.CONFIG_SCREEN_SUPPLIER);

		DEFERRED_SOUND_EVENTS.register(bus);
		ItemProviderType.DEFERRED_PROVIDER_TYPES.register(bus);
		ActionConditionType.DEFERRED_ACTION_CONDITION_TYPES.register(bus);
		ActionFunctionType.DEFERRED_FUNCTION_TYPES.register(bus);
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
		SyncActionsMessage actionsMsg = ActionsManager.getSyncMessage();

		if (event.getPlayer() != null) {
			ServerPlayer player = event.getPlayer();
			if (!player.connection.connection.isMemoryConnection()) {
				ActionsNetwork.sendToPlayer(player, actionsMsg);
			}
		} else {
			PlayerList playerList = event.getPlayerList();
			if (!playerList.getServer().isSingleplayer()) {
				playerList.getPlayers().forEach(player -> {
					ActionsNetwork.sendToPlayer(player, actionsMsg);
				});
			}
		}
	}

}
