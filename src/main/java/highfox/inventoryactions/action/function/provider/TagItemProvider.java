package highfox.inventoryactions.action.function.provider;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.itemprovider.ItemProviderType;
import highfox.inventoryactions.api.itemprovider.LootFunctionsProvider;
import highfox.inventoryactions.api.util.LootParams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public class TagItemProvider extends LootFunctionsProvider {
	private final TagKey<Item> tagKey;
	private final Optional<NumberProvider> amountProvider;
	private final Supplier<ObjectArrayList<Item>> tagContents;

	public TagItemProvider(LootItemFunction[] modifiers, LootParams params, TagKey<Item> tagKey, Optional<NumberProvider> amountProvider) {
		super(modifiers, params);
		this.tagKey = tagKey;
		this.amountProvider = amountProvider;
		this.tagContents = Suppliers.memoize(() -> this.getTag().stream().collect(ObjectArrayList.toList()));
	}

	@Override
	public void addItems(IActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		if (this.amountProvider.isPresent()) {
			int times = this.amountProvider.get().getInt(context.getLootContext());

			for (int i = 0; i < times; i++) {
				results.add(this.getRandomItem(context, random));
			}
		} else {
			this.tagContents.get().stream().map(ItemStack::new).forEach(stack -> results.add(this.applyModifiers(context, stack)));
		}
	}

	public ItemStack getRandomItem(IActionContext context, RandomSource random) {
		return Util.getRandomSafe(this.tagContents.get(), random).map(item -> {
			ItemStack result = new ItemStack(item);
			return this.applyModifiers(context, result);
		}).orElse(ItemStack.EMPTY);
	}

	private ITag<Item> getTag() {
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		return tags.getTag(this.tagKey);
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderTypes.TAG.get();
	}

	public static class Deserializer extends BaseSerializer<TagItemProvider> {

		@Override
		public TagItemProvider fromJson(JsonObject json, JsonDeserializationContext context, LootItemFunction[] functions, LootParams params) {
			TagKey<Item> tagKey = TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(GsonHelper.getAsString(json, "tag")));
			Optional<NumberProvider> amountProvider = Optional.ofNullable(GsonHelper.getAsObject(json, "amount", null, context, NumberProvider.class));

			return new TagItemProvider(functions, params, tagKey, amountProvider);
		}

	}

}
