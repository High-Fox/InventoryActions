package highfox.inventoryactions.action.function.provider;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemMap;
import highfox.inventoryactions.util.ItemSource;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class MappedItemProvider extends ItemFunctionsProvider implements ISingleItemResult {
	public static final Codec<MappedItemProvider> CODEC = RecordCodecBuilder.create(instance -> functionsCodec(instance).and(instance.group(
			ItemSource.CODEC.fieldOf("source").forGetter(o -> o.source),
			ItemMap.CODEC.fieldOf("item_map").forGetter(o -> o.itemMap)
	)).apply(instance, MappedItemProvider::new));

	private final ItemSource source;
	private final ItemMap itemMap;

	public MappedItemProvider(LootItemFunction[] modifiers, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState, ItemSource source, ItemMap itemMap) {
		super(modifiers, tool, blockState);
		this.source = source;
		this.itemMap = itemMap;
	}

	@Override
	public ItemStack getItem(ActionContext context, RandomSource random) {
		Holder<Item> item = this.source.get(context).getItemHolder();
		Optional<Holder<Item>> holder = Optional.ofNullable(this.itemMap.getValue(item));

		if (holder.isPresent()) {
			return this.applyModifiers(context, new ItemStack(holder.get()));
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderType.MAPPED.get();
	}

}
