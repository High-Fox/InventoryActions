package highfox.inventoryactions.action.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.NbtUtils;
import highfox.inventoryactions.util.ItemSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemNbtCondition extends ItemSourcingCondition {
	public static final Codec<ItemNbtCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			CompoundTag.CODEC.fieldOf("nbt").forGetter(o -> o.nbt)
	).apply(instance, ItemNbtCondition::new));

	private final CompoundTag nbt;

	public ItemNbtCondition(ItemSource source, CompoundTag nbt) {
		super(source);
		this.nbt = nbt;
	}

	@Override
	public boolean test(ActionContext context) {
		ItemStack stack = this.source.get(context);
		CompoundTag itemNbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
		itemNbt.putByte("Count", (byte)stack.getCount());

		return NbtUtils.compareNbt(this.nbt, itemNbt);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ITEM_NBT.get();
	}

}
