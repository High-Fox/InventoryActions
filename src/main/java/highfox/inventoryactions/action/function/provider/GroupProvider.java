package highfox.inventoryactions.action.function.provider;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class GroupProvider extends ItemFunctionsProvider {
	public static final Codec<GroupProvider> CODEC = RecordCodecBuilder.create(instance -> functionsCodec(instance).and(
			IItemProvider.CODEC.listOf().fieldOf("providers").forGetter(o -> o.providers)
	).apply(instance, GroupProvider::new));

	private final List<IItemProvider> providers;

	public GroupProvider(LootItemFunction[] functions, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState, List<IItemProvider> providers) {
		super(functions, tool, blockState);
		this.providers = providers;
	}

	@Override
	public void addItems(ActionContext context, Random random, ObjectArrayList<ItemStack> results) {
		ObjectArrayList<ItemStack> toModify = new ObjectArrayList<ItemStack>();
		for (IItemProvider provider : this.providers) {
			provider.addItems(context, random, provider instanceof ItemFunctionsProvider ? toModify : results);
		}

		for (ItemStack stack : toModify) {
			this.applyModifiers(context, stack);
		}

		results.addAll(toModify);
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderType.GROUP.get();
	}

}
