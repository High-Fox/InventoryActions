package highfox.inventoryactions.util;

import java.lang.reflect.Type;
import java.util.Optional;
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
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

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

	public static BlockState convertToBlockState(JsonElement json) {
		JsonObject object = GsonHelper.convertToJsonObject(json, "blockstate");
		ResourceLocation blockName = new ResourceLocation(GsonHelper.getAsString(object, "Name"));
		Block block = ForgeRegistries.BLOCKS.getDelegate(blockName).map(Reference::get).orElseThrow(() -> {
			return new IllegalArgumentException("Unknown block " + blockName);
		});
		BlockState state = block.defaultBlockState();
		JsonObject propertiesObject = GsonHelper.getAsJsonObject(object, "Properties", null);
		if (propertiesObject != null && !propertiesObject.isJsonNull()) {
			StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
			for (String propertyName : propertiesObject.keySet()) {
				Property<?> property = stateDefinition.getProperty(propertyName);
				if (property != null) {
					state = setBlockStateValue(state, property, propertyName, blockName, propertiesObject);
				} else {
					throw new IllegalArgumentException("Unknown blockstate property: " + propertyName + " for block: " + blockName);
				}
			}
		}

		return state;

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

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setBlockStateValue(S stateHolder, Property<T> property, String propertyName, ResourceLocation blockName, JsonObject json) {
		Optional<T> value = property.getValue(GsonHelper.getAsString(json, propertyName));
		if (value.isPresent()) {
			return stateHolder.setValue(property, value.get());
		} else {
			throw new IllegalArgumentException("Invalid value: " + GsonHelper.getAsString(json, propertyName) + " for property: " + propertyName + " of block: " + blockName);
		}
	}

}
