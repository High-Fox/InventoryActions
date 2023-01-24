package highfox.inventoryactions.util;

import java.lang.reflect.Type;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.itemprovider.IItemProvider;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

@SuppressWarnings("serial")
public class SerializationUtils {
	public static final Type CONDITION_LIST_TYPE = new TypeToken<ImmutableList<IActionCondition>>() {}.getType();
	public static final Type FUNCTION_LIST_TYPE = new TypeToken<ImmutableList<IActionFunction>>() {}.getType();
	public static final Type ITEM_PROVIDER_LIST_TYPE = new TypeToken<ImmutableList<IItemProvider>>() {}.getType();

	public static Pattern getAsPattern(JsonObject json, String memberName) {
		String regex = GsonHelper.getAsString(json, memberName);
		try {
			return Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			throw new JsonSyntaxException("Invalid regex pattern: " + regex);
		}
	}

	@Nullable
	public static Pattern getAsPattern(JsonObject json, String memberName, @Nullable Pattern fallback) {
		return json.has(memberName) ? getAsPattern(json, memberName) : fallback;
	}

	public static MinMaxBounds.Ints getAsIntRange(JsonObject json, String memberName) {
		if (json.has(memberName)) {
			return convertToIntRange(json.get(memberName), memberName);
		} else {
			throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int or JsonObject");
		}
	}

	public static MinMaxBounds.Ints convertToIntRange(JsonElement json, String memberName) {
		if (GsonHelper.isNumberValue(json)) {
			return MinMaxBounds.Ints.exactly(GsonHelper.convertToInt(json, memberName));
		} else if (json.isJsonObject()) {
			JsonObject object = json.getAsJsonObject();
			Integer minimum = object.has("minimum") ? Integer.valueOf(GsonHelper.getAsInt(object, "minimum")) : null;
			Integer maximum = object.has("maximum") ? Integer.valueOf(GsonHelper.getAsInt(object, "maximum")) : null;
			if (minimum == null && maximum == null) {
				throw new JsonSyntaxException("Missing minimum or maximum value in " + memberName);
			} else {
				return new MinMaxBounds.Ints(minimum, maximum);
			}
		} else {
			throw new JsonSyntaxException("Expected " + memberName + " to be an Int or JsonObjecy, was " + GsonHelper.getType(json));
		}
	}

}
