package highfox.inventoryactions.action.function.provider;

import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemmap.ItemMap;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.itemprovider.LootFunctionsProvider;
import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.api.itemsource.ItemSources;
import highfox.inventoryactions.api.util.LootParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

public class MappedItemProvider extends LootFunctionsProvider {
	private final IItemSource source;
	private final ItemMap itemMap;

	public MappedItemProvider(LootItemFunction[] modifiers, LootParams params, IItemSource source, ItemMap itemMap) {
		super(modifiers, params);
		this.source = source;
		this.itemMap = itemMap;
	}

	@Override
	public void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		ResourceLocation itemName = this.source.get(context).getItemHolder().unwrap().map(ResourceKey::location, ForgeRegistries.ITEMS::getKey);
		Optional<Holder<Item>> holder = Optional.ofNullable(this.itemMap.getItemValue(itemName));

		if (holder.isPresent()) {
			results.add(this.applyModifiers(context, new ItemStack(holder.get())));
		}
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderTypes.MAPPED.get();
	}

	public static class Deserializer extends BaseSerializer<MappedItemProvider> {

		@Override
		public MappedItemProvider fromJson(JsonObject json, JsonDeserializationContext context, LootItemFunction[] functions, LootParams params) {
			IItemSource source = ItemSources.fromJson(json.get("source"));
			ItemMap itemMap = GsonHelper.getAsObject(json, "item_map", context, ItemMap.class);

			return new MappedItemProvider(functions, params, source, itemMap);
		}

	}

}
