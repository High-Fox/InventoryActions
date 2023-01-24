package highfox.inventoryactions.action.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemmap.ItemMap;
import highfox.inventoryactions.api.itemsource.IItemSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemMapCondition extends ItemSourcingCondition {
	private final ItemMap itemMap;
	private final boolean checkValues;

	public ItemMapCondition(IItemSource source, ItemMap itemMap, boolean checkValues) {
		super(source);
		this.itemMap = itemMap;
		this.checkValues = checkValues;
	}

	@Override
	public boolean test(IActionContext context) {
		ResourceLocation itemName = this.source.get(context).getItemHolder().unwrap().map(ResourceKey::location, ForgeRegistries.ITEMS::getKey);

		return this.checkValues ? this.itemMap.containsValue(itemName) : this.itemMap.containsKey(itemName);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.ITEM_MAP.get();
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		this.itemMap.toNetwork(buffer);
		buffer.writeBoolean(this.checkValues);
	}

	public static class Deserializer extends BaseDeserializer<ItemMapCondition> {

		@Override
		public ItemMapCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			ItemMap itemMap = GsonHelper.getAsObject(json, "item_map", context, ItemMap.class);
			boolean checkValues = GsonHelper.getAsBoolean(json, "check_values", false);

			return new ItemMapCondition(source, itemMap, checkValues);
		}

		@Override
		public ItemMapCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			ItemMap itemMap = ItemMap.fromNetwork(buffer);
			boolean checkValues = buffer.readBoolean();

			return new ItemMapCondition(source, itemMap, checkValues);
		}

	}

}
