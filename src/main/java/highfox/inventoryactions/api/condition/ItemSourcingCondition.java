package highfox.inventoryactions.api.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.api.itemsource.ItemSources;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Base class for a condition that requires an {@link IItemSource}
 */
public abstract class ItemSourcingCondition implements IActionCondition {
	protected final IItemSource source;

	public ItemSourcingCondition(IItemSource source) {
		this.source = source;
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		this.source.toNetwork(buffer);
		this.additionalToNetwork(buffer);
	}

	/**
	 * Saves additional data to a network buffer
	 *
	 * @param buffer a network buffer
	 */
	public abstract void additionalToNetwork(FriendlyByteBuf buffer);

	protected abstract static class BaseDeserializer<T extends ItemSourcingCondition> implements IDeserializer<T> {
		@Override
		public T fromJson(JsonObject json, JsonDeserializationContext context) {
			IItemSource source = ItemSources.fromJson(json.get("source"));
			return this.fromJson(json, context, source);
		}

		@Override
		public T fromNetwork(FriendlyByteBuf buffer) {
			IItemSource source = ItemSources.fromNetwork(buffer);
			return this.fromNetwork(buffer, source);
		}

		public abstract T fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source);

		public abstract T fromNetwork(FriendlyByteBuf buffer, IItemSource source);
	}

}
