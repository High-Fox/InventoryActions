package highfox.inventoryactions.action.function.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemprovider.IItemProvider;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.itemprovider.LootFunctionsProvider;
import highfox.inventoryactions.api.util.LootParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class GroupProvider extends LootFunctionsProvider {
	private final IItemProvider[] providers;

	public GroupProvider(LootItemFunction[] functions, LootParams params, IItemProvider[] providers) {
		super(functions, params);
		this.providers = providers;
	}

	@Override
	public void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		ObjectArrayList<ItemStack> toModify = new ObjectArrayList<ItemStack>();
		for (IItemProvider provider : this.providers) {
			provider.addItems(context, random, provider instanceof LootFunctionsProvider ? toModify : results);
		}

		for (ItemStack stack : toModify) {
			this.applyModifiers(context, stack);
		}

		results.addAll(toModify);
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderTypes.GROUP.get();
	}

	public static class Deserializer extends BaseSerializer<GroupProvider> {

		@Override
		public GroupProvider fromJson(JsonObject json, JsonDeserializationContext context, LootItemFunction[] functions, LootParams params) {
			IItemProvider[] providers = GsonHelper.getAsObject(json, "providers", context, IItemProvider[].class);

			return new GroupProvider(functions, params, providers);
		}

	}

}
