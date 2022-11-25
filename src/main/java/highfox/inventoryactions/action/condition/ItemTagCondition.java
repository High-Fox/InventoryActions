package highfox.inventoryactions.action.condition;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemTagCondition extends ItemSourcingCondition {
	public static final Codec<ItemTagCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			TagKey.codec(ForgeRegistries.Keys.ITEMS).listOf().fieldOf("tags").forGetter(o -> o.tags)
	).apply(instance, ItemTagCondition::new));

	private final List<TagKey<Item>> tags;

	public ItemTagCondition(ItemSource source, List<TagKey<Item>> tags) {
		super(source);
		this.tags = tags;
	}

	@Override
	public boolean test(ActionContext context) {
		ItemStack stack = this.source.get(context);

		return this.tags.stream().anyMatch(stack::is);
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ITEM_TAG.get();
	}

}
