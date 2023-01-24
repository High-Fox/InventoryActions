package highfox.inventoryactions.util;

import java.lang.reflect.Type;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;

import highfox.inventoryactions.api.serialization.TypeDeserializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;

// Adapted from GsonAdapterFactory
public class DeserializerAdapterFactory {
	public static <E, T extends TypeDeserializer<E>> Builder<E, T> builder(IForgeRegistry<T> registry, String elementName, String typeKey, Function<E, T> typeGetter) {
		return new Builder<>(registry, elementName, typeKey, typeGetter);
	}

	public static class Builder<E, T extends TypeDeserializer<E>> {
		private final IForgeRegistry<T> registry;
		private final String elementName;
		private final String typeKey;
		private final Function<E, T> typeGetter;
		@Nullable
		private Pair<T, InlineDeserializer<? extends E>> inlineType;
		@Nullable
		private T defaultType;

		protected Builder(IForgeRegistry<T> registry, String elementName, String typeKey, Function<E, T> typeGetter) {
			this.registry = registry;
			this.elementName = elementName;
			this.typeKey = typeKey;
			this.typeGetter = typeGetter;
		}

		public Builder<E, T> withInlineDeserializer(T inlineType, InlineDeserializer<? extends E> inlineSerializer) {
			this.inlineType = Pair.of(inlineType, inlineSerializer);
			return this;
		}

		public Builder<E, T> withDefaultType(T defaultType) {
			this.defaultType = defaultType;
			return this;
		}

		public Object build() {
			return new JsonAdapter<>(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType, this.inlineType);
		}
	}

	public interface InlineDeserializer<T> {
		T deserialize(JsonElement json, JsonDeserializationContext context);
	}

	protected static class JsonAdapter<E, T extends TypeDeserializer<E>> implements JsonDeserializer<E> {
		private final IForgeRegistry<T> registry;
		private final String elementName;
		private final String typeKey;
		private final Function<E, T> typeGetter;
		@Nullable
		private final T defaultType;
		@Nullable
		private final Pair<T, InlineDeserializer<? extends E>> inlineType;

		protected JsonAdapter(IForgeRegistry<T> registry, String elementName, String typeKey, Function<E, T> typeGetter, @Nullable T defaultType, @Nullable Pair<T, InlineDeserializer<? extends E>> inlineType) {
			this.registry = registry;
			this.elementName = elementName;
			this.typeKey = typeKey;
			this.typeGetter = typeGetter;
			this.defaultType = defaultType;
			this.inlineType = inlineType;
		}

		@Override
		public E deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject object = GsonHelper.convertToJsonObject(json, this.elementName);
				String s = GsonHelper.getAsString(object, this.typeKey, "");
				T t;
				if (s.isEmpty()) {
					t = this.defaultType;
				} else {
					ResourceLocation resourcelocation = new ResourceLocation(s);
					t = this.registry.getValue(resourcelocation);
				}

				if (t == null) {
					throw new JsonSyntaxException("Unknown type '" + s + "'");
				} else {
					return t.getDeserializer().fromJson(object, context);
				}
			} else if (this.inlineType == null) {
				throw new UnsupportedOperationException("Object " + json + " can't be deserialized");
			} else {
				return this.inlineType.getSecond().deserialize(json, context);
			}
		}
	}

}
