package highfox.inventoryactions.api.condition;

import highfox.inventoryactions.api.action.IActionContext;
import net.minecraft.network.FriendlyByteBuf;

public interface IActionCondition {

	/**
	 * Returns true if this condition passes with the given context
	 * <p>
	 * This is run on both the server and client, so it must be thread safe and return the same result on both sides
	 *
	 * @param context the context this condition is being tested from
	 * @return true if this condition passes
	 */
	boolean test(IActionContext context);

	/**
	 * Gets the deserializer type
	 *
	 * @return an {@link ActionConditionType}
	 */
	ActionConditionType getType();

	/**
	 * Writes this condition to a network buffer
	 *
	 * @param buffer a network buffer
	 */
	void toNetwork(FriendlyByteBuf buffer);
}
