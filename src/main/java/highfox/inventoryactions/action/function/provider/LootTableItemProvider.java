package highfox.inventoryactions.action.function.provider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemprovider.IItemProvider;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.util.LootParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableItemProvider implements IItemProvider {
	private final ResourceLocation tableLocation;
	private final LootParams params;

	public LootTableItemProvider(ResourceLocation tableLocation, LootParams params) {
		this.tableLocation = tableLocation;
		this.params = params;
	}

	@Override
	public void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		LootContext lootContext = context.getLootContext(this.params.getTool(context), this.params.getBlockState());
		LootTable table = context.getLevel().getServer().getLootTables().get(this.tableLocation);
		if (table == LootTable.EMPTY) {
			throw new IllegalArgumentException("Unknown loot table: " + this.tableLocation);
		}

		ObjectArrayList<ItemStack> rollResults = table.getRandomItems(lootContext);
		results.addAll(rollResults);
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderTypes.LOOT_TABLE.get();
	}

	public static class Deserializer implements IDeserializer<LootTableItemProvider> {

		@Override
		public LootTableItemProvider fromJson(JsonObject json, JsonDeserializationContext context) {
			ResourceLocation tableLocation = new ResourceLocation(GsonHelper.getAsString(json, "loot_table"));
			LootParams params = LootParams.fromJson(json);

			return new LootTableItemProvider(tableLocation, params);
		}

		@Override
		public LootTableItemProvider fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}

}
