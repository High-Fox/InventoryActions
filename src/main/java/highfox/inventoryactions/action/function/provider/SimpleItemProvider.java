package highfox.inventoryactions.action.function.provider;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

public class SimpleItemProvider extends ItemFunctionsProvider implements ISingleItemResult {
	public static final Codec<SimpleItemProvider> CODEC = RecordCodecBuilder.create(instance -> functionsCodec(instance).and(
			RegistryFileCodec.create(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS.getCodec()).fieldOf("item").forGetter(o -> o.item)
	).apply(instance, SimpleItemProvider::new));

	protected final Holder<Item> item;

	public SimpleItemProvider(LootItemFunction[] modifiers, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState, Holder<Item> item) {
		super(modifiers, tool, blockState);
		this.item = item;
	}

	@Override
	public ItemStack getItem(ActionContext context, RandomSource random) {
		return this.applyModifiers(context, new ItemStack(this.item));
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderType.SIMPLE.get();
	}

}
