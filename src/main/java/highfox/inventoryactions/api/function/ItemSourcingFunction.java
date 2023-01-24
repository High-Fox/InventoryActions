package highfox.inventoryactions.api.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.api.itemsource.ItemSources;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Base class for a function that requires an {@link IItemSource}
 */
public abstract class ItemSourcingFunction implements IActionFunction {
	protected final IItemSource source;

	public ItemSourcingFunction(IItemSource source) {
		this.source = source;
	}

	/**
	 * Base deserializer for an item sourcing function
	 *
	 * @param <T> the item sourcing function
	 */
	protected abstract static class BaseDeserializer<T extends ItemSourcingFunction> implements IDeserializer<T> {
		@Override
		public T fromJson(JsonObject json, JsonDeserializationContext context) {
			IItemSource source = ItemSources.fromJson(json.get("source"));
			return this.fromJson(json, context, source);
		}

		@Override
		public T fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

		public abstract T fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source);
	}

}
