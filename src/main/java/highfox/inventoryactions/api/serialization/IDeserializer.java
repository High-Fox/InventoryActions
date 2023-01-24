package highfox.inventoryactions.api.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Describes an object that can be deserialized from JSON or a network buffer.
 */
public interface IDeserializer<T> {

	/**
	 * Deserializes from a JSON object
	 *
	 * @param json the json object
	 * @param context the deserialization context
	 * @return the deserialized object
	 */
	T fromJson(JsonObject json, JsonDeserializationContext context);

	/**
	 * Deserializes from a network buffer
	 * <p>
	 * If this method won't ever be called (such as with {@code IActionFunction}), it is
	 * recommended to throw an {@code UnsupportedOperationException}
	 *
	 * @param buffer the network buffer
	 * @return the deserialized object
	 */
	T fromNetwork(FriendlyByteBuf buffer);
}
