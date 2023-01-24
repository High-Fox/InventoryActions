package highfox.inventoryactions.api.itemprovider;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.action.function.ApplyModifiersFunction;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.util.LootParams;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;

/**
 * Base class for an item provider that allows {@link LootItemFunctions} to be applied to it's items
 */
public abstract class LootFunctionsProvider implements IItemProvider {
	protected final LootItemFunction[] functions;
	protected final LootParams params;

	public LootFunctionsProvider(LootItemFunction[] functions, LootParams params) {
		this.functions = functions;
		this.params = params;
	}

	/**
	 * Applies the loot item functions to an itemstack
	 *
	 * @param context the action context
	 * @param stack the itemstack to apply the functions to
	 * @return the modified itemstack
	 */
	protected ItemStack applyModifiers(IActionContext context, ItemStack stack) {
		LootContext lootContext = context.getLootContext(this.params.getTool(context), this.params.getBlockState());
		List<LootItemFunction> validFunctions = ApplyModifiersFunction.validateContextParams(Arrays.asList(this.functions));

		return ApplyModifiersFunction.applyModifiers(stack, lootContext, validFunctions);
	}

	/**
	 * Base deserializer for a function that uses loot item functions
	 *
	 * @param <T> the function with loot item functions
	 */
	protected static abstract class BaseSerializer<T extends LootFunctionsProvider> implements IDeserializer<T> {

		@Override
		public T fromJson(JsonObject json, JsonDeserializationContext context) {
			LootItemFunction[] functions = GsonHelper.getAsObject(json, "functions", new LootItemFunction[0], context, LootItemFunction[].class);
			LootParams params = LootParams.fromJson(json);

			return this.fromJson(json, context, functions, params);
		}

		@Override
		public T fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

		public abstract T fromJson(JsonObject json, JsonDeserializationContext context, LootItemFunction[] functions, LootParams params);

	}

}