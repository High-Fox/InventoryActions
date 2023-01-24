package highfox.inventoryactions.action.condition;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.util.DeserializerAdapterFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ActionConditionTypes {
	public static final DeferredRegister<ActionConditionType> DEFERRED_CONDITION_SERIALIZERS = DeferredRegister.create(ActionConditionType.CONDITION_SERIALIZERS_KEY, ActionsConstants.MODID);
	public static final Supplier<IForgeRegistry<ActionConditionType>> CONDITION_SERIALIZERS = DEFERRED_CONDITION_SERIALIZERS.makeRegistry(
			() -> new RegistryBuilder<ActionConditionType>().disableSaving().disableOverrides());

	public static final RegistryObject<ActionConditionType> ITEM = register("item", new ItemCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> ITEM_TAG = register("item_tag", new ItemTagCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> ITEM_NBT = register("item_nbt", new ItemNbtCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> ITEM_MAP = register("item_map", new ItemMapCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> ITEM_TIER = register("item_tier", new ItemTierCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> TOOL = register("tool", new ItemToolCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> PLAYER = register("player", new PlayerCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> INVERT = register("invert", new InvertCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> AND = register("and", new AndCondition.Deserializer());
	public static final RegistryObject<ActionConditionType> OR = register("or", new OrCondition.Deserializer());

	@Nullable
	public static ResourceLocation getRegistryName(ActionConditionType type) {
		return CONDITION_SERIALIZERS.get().getKey(type);
	}

	private static RegistryObject<ActionConditionType> register(String name, IDeserializer<? extends IActionCondition> deserializer) {
		return DEFERRED_CONDITION_SERIALIZERS.register(name, () -> new ActionConditionType(deserializer));
	}

	public static Object createTypeAdapater() {
		return DeserializerAdapterFactory.builder(ActionConditionTypes.CONDITION_SERIALIZERS.get(), "condition", "type", IActionCondition::getType).build();
	}

}
