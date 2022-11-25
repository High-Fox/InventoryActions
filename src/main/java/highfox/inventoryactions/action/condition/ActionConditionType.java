package highfox.inventoryactions.action.condition;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.InventoryActions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ActionConditionType extends ForgeRegistryEntry<ActionConditionType> {
	public static final DeferredRegister<ActionConditionType> DEFERRED_ACTION_CONDITION_TYPES = DeferredRegister.create(new ResourceLocation(InventoryActions.MODID, "condition_types"), InventoryActions.MODID);
	public static final Supplier<IForgeRegistry<ActionConditionType>> CONDITION_TYPES = DEFERRED_ACTION_CONDITION_TYPES.makeRegistry(ActionConditionType.class,
			() -> new RegistryBuilder<ActionConditionType>().disableSaving().disableOverrides());

	public static final RegistryObject<ActionConditionType> ITEM = register("item", ItemCondition.CODEC);
	public static final RegistryObject<ActionConditionType> ITEM_TAG = register("item_tag", ItemTagCondition.CODEC);
	public static final RegistryObject<ActionConditionType> ITEM_NBT = register("item_nbt", ItemNbtCondition.CODEC);
	public static final RegistryObject<ActionConditionType> ITEM_MAP = register("item_map", ItemMapCondition.CODEC);
	public static final RegistryObject<ActionConditionType> ITEM_TIER = register("item_tier", ItemTierCondition.CODEC);
	public static final RegistryObject<ActionConditionType> TOOL = register("tool", ToolCondition.CODEC);
	public static final RegistryObject<ActionConditionType> PLAYER = register("player", PlayerCondition.CODEC);

	public static final RegistryObject<ActionConditionType> INVERT = register("invert", InvertCondition.CODEC);
	public static final RegistryObject<ActionConditionType> AND = register("and", AndCondition.CODEC);
	public static final RegistryObject<ActionConditionType> ANY = register("or", OrCondition.CODEC);

	private static RegistryObject<ActionConditionType> register(String name, Codec<? extends IActionCondition> codec) {
		return DEFERRED_ACTION_CONDITION_TYPES.register(name, () -> new ActionConditionType(codec));
	}

	private final Codec<? extends IActionCondition> codec;

	public ActionConditionType(Codec<? extends IActionCondition> codec) {
		this.codec = codec;
	}

	public Codec<? extends IActionCondition> getCodec() {
		return this.codec;
	}

}
