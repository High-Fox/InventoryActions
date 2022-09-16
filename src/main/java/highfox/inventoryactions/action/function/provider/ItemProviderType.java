package highfox.inventoryactions.action.function.provider;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.InventoryActions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ItemProviderType {
	public static final DeferredRegister<ItemProviderType> DEFERRED_PROVIDER_TYPES = DeferredRegister.create(new ResourceLocation(InventoryActions.MODID, "item_provider_types"), InventoryActions.MODID);
	public static final Supplier<IForgeRegistry<ItemProviderType>> PROVIDER_TYPES = DEFERRED_PROVIDER_TYPES.makeRegistry(
			() -> new RegistryBuilder<ItemProviderType>().disableSaving().disableOverrides());

	public static final RegistryObject<ItemProviderType> SIMPLE = register("simple", SimpleItemProvider.CODEC);
	public static final RegistryObject<ItemProviderType> GROUP = register("group", GroupProvider.CODEC);
	public static final RegistryObject<ItemProviderType> MAPPED = register("mapped", MappedItemProvider.CODEC);
	public static final RegistryObject<ItemProviderType> TAG = register("tag", TagItemProvider.CODEC);
	public static final RegistryObject<ItemProviderType> LOOT_TABLE = register("loot_table", LootTableItemProvider.CODEC);

	public static RegistryObject<ItemProviderType> register(String name, Codec<? extends IItemProvider> codec) {
		return DEFERRED_PROVIDER_TYPES.register(name, () -> new ItemProviderType(codec));
	}

	private final Codec<? extends IItemProvider> codec;

	public ItemProviderType(Codec<? extends IItemProvider> codec) {
		this.codec = codec;
	}

	public Codec<? extends IItemProvider> getCodec() {
		return this.codec;
	}

}
