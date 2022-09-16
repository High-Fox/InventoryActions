package highfox.inventoryactions.action.function;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.util.ItemSource;

public abstract class ItemSourcingFunction implements IActionFunction {
	protected final ItemSource source;

	public ItemSourcingFunction(ItemSource source) {
		this.source = source;
	}

	public static <T extends ItemSourcingFunction> Products.P1<RecordCodecBuilder.Mu<T>, ItemSource> sourceCodec(RecordCodecBuilder.Instance<T> instance) {
		return instance.group(ItemSource.CODEC.fieldOf("source").forGetter(o -> o.source));
	}

}
