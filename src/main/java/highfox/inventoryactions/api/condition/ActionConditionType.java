package highfox.inventoryactions.api.condition;

import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.serialization.TypeDeserializer;
import highfox.inventoryactions.api.util.ActionsConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/**
 * A type of condition serializer
 */
public class ActionConditionType extends TypeDeserializer<IActionCondition> {
	/**
	 * The condition serializers registry key. Use with {@link DeferredRegister} to
	 * register custom conditions
	 *
	 * <pre>
	 * DeferredRegister.create(ActionConditionType.CONDITION_SERIALIZERS_KEY, modid);
	 * </pre>
	 */
	public static final ResourceKey<Registry<ActionConditionType>> CONDITION_SERIALIZERS_KEY = ResourceKey.createRegistryKey(new ResourceLocation(ActionsConstants.MODID, "condition_serializers"));

	/**
	 * Constructs a condition deserializer type
	 *
	 * @param deserializer the condition deserializer
	 */
	public ActionConditionType(IDeserializer<? extends IActionCondition> deserializer) {
		super(deserializer);
	}

}
