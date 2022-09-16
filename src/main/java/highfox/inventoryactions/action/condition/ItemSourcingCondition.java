package highfox.inventoryactions.action.condition;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.util.ItemSource;

public abstract class ItemSourcingCondition implements IActionCondition {
	protected final ItemSource source;

	public ItemSourcingCondition(ItemSource source) {
		this.source = source;
	}

	public static <T extends ItemSourcingCondition> Products.P1<RecordCodecBuilder.Mu<T>, ItemSource> sourceCodec(RecordCodecBuilder.Instance<T> instance) {
		return instance.group(ItemSource.CODEC.fieldOf("source").forGetter(o -> o.source));
	}

}
