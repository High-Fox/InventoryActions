package highfox.inventoryactions.action.function;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.InventoryActions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ActionFunctionType extends ForgeRegistryEntry<ActionFunctionType> {
	public static final DeferredRegister<ActionFunctionType> DEFERRED_FUNCTION_TYPES = DeferredRegister.create(new ResourceLocation(InventoryActions.MODID, "function_types"), InventoryActions.MODID);
	public static final Supplier<IForgeRegistry<ActionFunctionType>> FUNCTION_TYPES = DEFERRED_FUNCTION_TYPES.makeRegistry(ActionFunctionType.class,
			() -> new RegistryBuilder<ActionFunctionType>().disableSaving().disableOverrides());

	public static final RegistryObject<ActionFunctionType> CRAFTING = register("crafting", CraftingFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> CONDITIONAL = register("conditional", ConditionalFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> ALTERNATIVES = register("alternatives", AlternativesFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> APPLY_MODIFIERS = register("apply_modifiers", ApplyModifiersFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> SHRINK_ITEM = register("shrink_item", ShrinkItemFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> DAMAGE_ITEM = register("damage_item", DamageItemFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> GIVE_ITEMS = register("give_items", GiveItemsFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> GIVE_XP = register("give_xp", GiveXpFunction.CODEC);
	public static final RegistryObject<ActionFunctionType> PLAY_SOUND = register("play_sound", PlaySoundFunction.CODEC);

	private static RegistryObject<ActionFunctionType> register(String name, Codec<? extends IActionFunction> codec) {
		return DEFERRED_FUNCTION_TYPES.register(name, () -> new ActionFunctionType(codec));
	}

	private final Codec<? extends IActionFunction> codec;

	public ActionFunctionType(Codec<? extends IActionFunction> codec) {
		this.codec = codec;
	}

	public Codec<? extends IActionFunction> getCodec() {
		return this.codec;
	}

}
