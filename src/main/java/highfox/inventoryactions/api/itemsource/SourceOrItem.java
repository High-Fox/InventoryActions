package highfox.inventoryactions.api.itemsource;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;

import highfox.inventoryactions.api.action.IActionContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Contains either an item source or an item holder
 */
public record SourceOrItem(Either<IItemSource, Holder<Item>> either) {

	/**
	 * Resolves the contained item and returns it in an itemstack
	 *
	 * @param context an action context
	 * @return the resolved itemstack
	 */
	public ItemStack resolve(IActionContext context) {
		return this.either.map(source -> source.get(context), ItemStack::new);
	}

	public boolean isItemSource() {
		return this.either.left().isPresent();
	}

	public static SourceOrItem ofSource(IItemSource source) {
		return new SourceOrItem(Either.left(source));
	}

	public static SourceOrItem ofItem(Holder<Item> item) {
		return new SourceOrItem(Either.right(item));
	}

	/**
	 * Deserializes from a json element
	 *
	 * @param json the json element to deserialize
	 * @return the deserialized {@code SourceOrItem}
	 * @throws JsonSyntaxException if the json element isn't a string, or it is neither a valid item name or item source
	 */
	public static SourceOrItem fromJson(JsonElement json) {
		String name = GsonHelper.convertToString(json, "item");
		if (ItemSources.isValidSource(name)) {
			return SourceOrItem.ofSource(ItemSources.getSource(name));
		} else {
			return SourceOrItem.ofItem(ForgeRegistries.ITEMS.getDelegate(new ResourceLocation(name)).orElseThrow(() -> {
				return new JsonSyntaxException(name + " is neither a valid item name or valid item source");
			}));
		}
	}
}