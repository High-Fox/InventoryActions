package highfox.inventoryactions.action.function.provider;

import java.util.function.Supplier;

import highfox.inventoryactions.api.itemprovider.IItemProvider;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.util.DeserializerAdapterFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ItemProviderTypes {
	public static final DeferredRegister<ItemProviderType> DEFERRED_PROVIDER_SERIALIZERS = DeferredRegister.create(ItemProviderType.ITEM_PROVIDER_SERIALIZERS_KEY, ActionsConstants.MODID);
	public static final Supplier<IForgeRegistry<ItemProviderType>> PROVIDER_SERIALIZERS = DEFERRED_PROVIDER_SERIALIZERS.makeRegistry(
			() -> new RegistryBuilder<ItemProviderType>().disableSaving().disableOverrides().disableSync());

	public static final RegistryObject<ItemProviderType> SIMPLE = register("simple", new SimpleItemProvider.Deserializer());
	public static final RegistryObject<ItemProviderType> GROUP = register("group", new GroupProvider.Deserializer());
	public static final RegistryObject<ItemProviderType> MAPPED = register("mapped", new MappedItemProvider.Deserializer());
	public static final RegistryObject<ItemProviderType> TAG = register("tag", new TagItemProvider.Deserializer());
	public static final RegistryObject<ItemProviderType> LOOT_TABLE = register("loot_table", new LootTableItemProvider.Deserializer());

	public static RegistryObject<ItemProviderType> register(String name, IDeserializer<? extends IItemProvider> deserializer) {
		return DEFERRED_PROVIDER_SERIALIZERS.register(name, () -> new ItemProviderType(deserializer));
	}

	public static Object createTypeAdapter() {
		return DeserializerAdapterFactory.builder(PROVIDER_SERIALIZERS.get(), "item provider", "type", IItemProvider::getType).withDefaultType(SIMPLE.get()).build();
	}


}
