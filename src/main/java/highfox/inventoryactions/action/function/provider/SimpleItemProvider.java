package highfox.inventoryactions.action.function.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.itemprovider.LootFunctionsProvider;
import highfox.inventoryactions.api.util.LootParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

public class SimpleItemProvider extends LootFunctionsProvider {
	protected final Holder<Item> item;

	public SimpleItemProvider(LootItemFunction[] modifiers, LootParams params, Holder<Item> item) {
		super(modifiers, params);
		this.item = item;
	}

	@Override
	public void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		results.add(this.applyModifiers(context, new ItemStack(this.item)));
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderTypes.SIMPLE.get();
	}

	public static class Deserializer extends BaseSerializer<SimpleItemProvider> {

		@Override
		public SimpleItemProvider fromJson(JsonObject json, JsonDeserializationContext context, LootItemFunction[] functions, LootParams params) {
			ResourceLocation itemName = new ResourceLocation(GsonHelper.getAsString(json, "item"));
			Reference<Item> holder = ForgeRegistries.ITEMS.getDelegate(itemName).orElseThrow(() -> {
				return new IllegalArgumentException("Unknown item: " + itemName);
			});

			return new SimpleItemProvider(functions, params, holder);
		}

	}

}
