package highfox.inventoryactions.action.condition;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemsource.IItemSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemTagCondition extends ItemSourcingCondition {
	private final List<TagKey<Item>> tags;

	public ItemTagCondition(IItemSource source, List<TagKey<Item>> tags) {
		super(source);
		this.tags = tags;
	}

	@Override
	public boolean test(IActionContext context) {
		ItemStack stack = this.source.get(context);

		return this.tags.stream().anyMatch(stack::is);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.ITEM_TAG.get();
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		buffer.writeCollection(this.tags, (buf, tag) -> buf.writeResourceLocation(tag.location()));
	}

	public static class Deserializer extends BaseDeserializer<ItemTagCondition> {

		@Override
		public ItemTagCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			JsonArray tagNames = GsonHelper.getAsJsonArray(json, "tags");

			ImmutableList.Builder<TagKey<Item>> tags = ImmutableList.builderWithExpectedSize(tagNames.size());
			for (JsonElement element : tagNames) {
				ResourceLocation name = new ResourceLocation(GsonHelper.convertToString(element, "tag"));

				tags.add(TagKey.create(ForgeRegistries.Keys.ITEMS, name));
			}

			return new ItemTagCondition(source, tags.build());
		}

		@Override
		public ItemTagCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			List<TagKey<Item>> tags = buffer.readList(buf -> TagKey.create(ForgeRegistries.Keys.ITEMS, buf.readResourceLocation()));

			return new ItemTagCondition(source, tags);
		}

	}

}
