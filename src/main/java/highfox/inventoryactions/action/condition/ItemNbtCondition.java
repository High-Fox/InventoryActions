package highfox.inventoryactions.action.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemsource.IItemSource;
import highfox.inventoryactions.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ItemNbtCondition extends ItemSourcingCondition {
	private final CompoundTag nbt;

	public ItemNbtCondition(IItemSource source, CompoundTag nbt) {
		super(source);
		this.nbt = nbt;
	}

	@Override
	public boolean test(IActionContext context) {
		ItemStack stack = this.source.get(context);
		CompoundTag itemNbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
		itemNbt.putByte("Count", (byte)stack.getCount());

		return NbtUtils.compareNbt(this.nbt, itemNbt);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.ITEM_NBT.get();
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		buffer.writeNbt(this.nbt);
	}

	public static class Deserializer extends BaseDeserializer<ItemNbtCondition> {

		@Override
		public ItemNbtCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			CompoundTag nbt = CraftingHelper.getNBT(GsonHelper.getAsJsonObject(json, "nbt"));

			return new ItemNbtCondition(source, nbt);
		}

		@Override
		public ItemNbtCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			CompoundTag nbt = buffer.readAnySizeNbt();

			return new ItemNbtCondition(source, nbt);
		}

	}

}
