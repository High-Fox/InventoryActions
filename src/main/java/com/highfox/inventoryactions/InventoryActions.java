package com.highfox.inventoryactions;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.highfox.inventoryactions.registries.SoundRegistry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(InventoryActions.MODID)
public class InventoryActions {
	public static final String MODID = "inventoryactions";
	public static final Logger LOG = LogManager.getLogger(InventoryActions.MODID);

	public InventoryActions() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ActionConfig.GENERAL_SPEC, "inventory-actions.toml");
        MinecraftForge.EVENT_BUS.register(this);
        SoundRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

}
