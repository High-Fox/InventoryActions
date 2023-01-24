package highfox.inventoryactions.action.function;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.serialization.IDeserializer;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.util.DeserializerAdapterFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ActionFunctionTypes {
	public static final DeferredRegister<ActionFunctionType> DEFERRED_FUNCTION_SERIALIZERS = DeferredRegister.create(ActionFunctionType.FUNCTION_SERIALIZERS_KEY, ActionsConstants.MODID);
	public static final Supplier<IForgeRegistry<ActionFunctionType>> FUNCTION_SERIALIZERS = DEFERRED_FUNCTION_SERIALIZERS.makeRegistry(
			() -> new RegistryBuilder<ActionFunctionType>().disableSaving().disableOverrides());

	public static final RegistryObject<ActionFunctionType> CRAFTING = register("crafting", new CraftingFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> CONDITIONAL = register("conditional", new ConditionalFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> ALTERNATIVES = register("alternatives", new AlternativesFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> APPLY_MODIFIERS = register("apply_modifiers", new ApplyModifiersFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> SHRINK_ITEM = register("shrink_item", new ShrinkItemFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> DAMAGE_ITEM = register("damage_item", new DamageItemFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> GIVE_ITEMS = register("give_items", new GiveItemsFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> GIVE_XP = register("give_xp", new GiveXpFunction.Deserializer());
	public static final RegistryObject<ActionFunctionType> PLAY_SOUND = register("play_sound", new PlaySoundFunction.Deserializer());

	@Nullable
	public static ResourceLocation getRegistryName(ActionFunctionType type) {
		return FUNCTION_SERIALIZERS.get().getKey(type);
	}

	private static RegistryObject<ActionFunctionType> register(String name, IDeserializer<? extends IActionFunction> deserializer) {
		return DEFERRED_FUNCTION_SERIALIZERS.register(name, () -> new ActionFunctionType(deserializer));
	}

	public static Object createTypeAdapater() {
		return DeserializerAdapterFactory.builder(ActionFunctionTypes.FUNCTION_SERIALIZERS.get(), "function", "type", IActionFunction::getType).build();
	}

}
