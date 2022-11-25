package highfox.inventoryactions.action.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemMap;
import highfox.inventoryactions.util.ItemSource;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemMapCondition extends ItemSourcingCondition {
	public static final Codec<ItemMapCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(instance.group(
			ItemMap.CODEC.fieldOf("item_map").forGetter(o -> o.itemMap),
			Codec.BOOL.optionalFieldOf("check_values", Boolean.valueOf(false)).forGetter(o -> o.checkValues)
	)).apply(instance, ItemMapCondition::new));

	private final ItemMap itemMap;
	private final boolean checkValues;

	public ItemMapCondition(ItemSource source, ItemMap itemMap, boolean checkValues) {
		super(source);
		this.itemMap = itemMap;
		this.checkValues = checkValues;
	}

	@Override
	public boolean test(ActionContext context) {
		Holder<Item> holder = ForgeRegistries.ITEMS.getHolder(this.source.get(context).getItem()).orElseThrow();

		return this.checkValues ? this.itemMap.containsValue(holder) : this.itemMap.containsKey(holder);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ITEM_MAP.get();
	}

}
