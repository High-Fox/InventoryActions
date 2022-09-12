package com.highfox.inventoryactions.registries;

import com.highfox.inventoryactions.InventoryActions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundRegistry {
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, InventoryActions.MODID);

	public static final RegistryObject<SoundEvent> solidify_concrete = SOUNDS.register("solidify_concrete", () -> new SoundEvent(new ResourceLocation(InventoryActions.MODID, "solidify_concrete_action")));

	public static void register(IEventBus bus) {
		SOUNDS.register(bus);
	}

}
