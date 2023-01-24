package highfox.inventoryactions.api.itemmap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.util.ActionsConstants;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * An immutable collection of mapped pairs of item names
 */
public class ItemMap {
	private final Object2ObjectMap<ResourceLocation, ResourceLocation> values;

	/**
	 * Constructs a new immutable item map with entries copied from the specified map
	 *
	 * @param map the map to copy
	 */
	public ItemMap(Map<ResourceLocation, ResourceLocation> map) {
		this.values = Object2ObjectMaps.unmodifiable(new Object2ObjectOpenHashMap<>(map));
	}

	/**
	 * Returns the value to which the given key is mapped
	 *
	 * @param key the key
	 * @return the corresponding value, or null if no value was present for the given key
	 * @see Map#get(Object)
	 */
	@Nullable
	public ResourceLocation getValue(ResourceLocation key) {
		return this.values.get(key);
	}

	/**
	 * Gets the value to which the given key is mapped, and returns a holder for the item
	 * registered to that value within {@link ForgeRegistries#ITEMS}
	 *
	 * @param key the key
	 * @return the associated item holder, or null if no value exists for the key
	 */
	@Nullable
	public Holder<Item> getItemValue(ResourceLocation key) {
		return ForgeRegistries.ITEMS.getDelegate(this.getValue(key)).orElse(null);
	}

	/**
	 * Returns true if this map contains a mapping for the specified key
	 *
	 * @param key the key
	 * @return true if this map associates a value to the given key
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(ResourceLocation key) {
		return this.values.containsKey(key);
	}

	/**
	 * Returns true if this map maps one or more keys to the specified value
	 *
	 * @param value the value
	 * @return true if this map maps one or more keys to given value
	 * @see Map#containsValue(Object)
	 */
	public boolean containsValue(ResourceLocation value) {
		return this.values.containsValue(value);
	}

	/**
	 * Returns the contained map
	 *
	 * @return the contained map
	 */
	public Map<ResourceLocation, ResourceLocation> map() {
		return this.values;
	}

	/**
	 * Returns the number of key-value mappings in this map
	 *
	 * @return the number of mappings in this map
	 * @see Map#size()
	 */
	public int size() {
		return this.values.size();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		ItemMap that = (ItemMap) obj;
		return Objects.equals(this.values, that.values);
	}

	/**
	 * Writes this item map to a network buffer
	 *
	 * @param buffer the network buffer
	 */
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeMap(this.map(), FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeResourceLocation);
	}

	/**
	 * Reads an item map from a network buffer
	 *
	 * @param buffer a network buffer
	 * @return the item map
	 */
	public static ItemMap fromNetwork(FriendlyByteBuf buffer) {
		Map<ResourceLocation, ResourceLocation> map = buffer.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readResourceLocation);

		return new ItemMap(map);
	}

	public static class Deserializer implements JsonDeserializer<ItemMap> {

		@Override
		public ItemMap deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			if (GsonHelper.isStringValue(json)) {
				ResourceLocation name = prefixLocation(ActionsConstants.MODID, GsonHelper.convertToString(json, "item map"));

				ItemMap itemMap = BuiltInItemMaps.getItemMap(name);

				if (itemMap == null) {
					throw new IllegalArgumentException("Unknown item map: " + name);
				} else {
					return itemMap;
				}
			} else if (json.isJsonObject()) {
				JsonObject object = json.getAsJsonObject();
				Map<ResourceLocation, ResourceLocation> map = new HashMap<>(object.size());
				json.getAsJsonObject().entrySet().stream().forEach(entry -> {
					ResourceLocation key = new ResourceLocation(entry.getKey());
					ResourceLocation value = new ResourceLocation(GsonHelper.convertToString(entry.getValue(), "item map value"));

					if (!ForgeRegistries.ITEMS.containsKey(key) || !ForgeRegistries.ITEMS.containsKey(value)) {
						throw new IllegalArgumentException("Unknown item: " + (!ForgeRegistries.ITEMS.containsKey(key) ? key : value));
					}

					map.put(key, value);
				});

				return new ItemMap(map);
			} else {
				throw new JsonSyntaxException("Expected item map to be an object or string, was " + GsonHelper.getType(json));
			}
		}

		private static ResourceLocation prefixLocation(String defaultNamespace, String location) {
			String[] parts = new String[] {defaultNamespace, location};
			int separatorIndex = location.indexOf(':');

			if (separatorIndex >= 0) {
				parts[1] = location.substring(separatorIndex + 1);
				if (separatorIndex >= 1) {
					parts[0] = location.substring(0, separatorIndex);
				}
			}

			return new ResourceLocation(parts[0], parts[1]);
		}

	}

}
