package highfox.inventoryactions.action.function.provider;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableItemProvider implements IItemProvider, IRequiresLootContext {
	public static final Codec<LootTableItemProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("loot_table").forGetter(o -> o.tableLocation)
	).and(IRequiresLootContext.paramsCodec(instance)).apply(instance, LootTableItemProvider::new));

	private final ResourceLocation tableLocation;
	private final Optional<Either<ItemSource, IItemProvider>> tool;
	private final Optional<BlockState> blockState;

	public LootTableItemProvider(ResourceLocation tableLocation, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState) {
		this.tableLocation = tableLocation;
		this.tool = tool;
		this.blockState = blockState;
	}

	@Override
	public void addItems(ActionContext context, RandomSource random, ObjectArrayList<ItemStack> results) {
		LootContext lootContext = context.getLootContext(this.getToolStack(context), this.blockState.orElse(null));
		LootTable table = context.getLevel().getServer().getLootTables().get(this.tableLocation);
		if (table == LootTable.EMPTY) {
			throw new IllegalArgumentException("Unknown loot table: " + this.tableLocation);
		}

		ObjectArrayList<ItemStack> rollResults = table.getRandomItems(lootContext);
		results.addAll(rollResults);
	}

	@Override
	public Optional<Either<ItemSource, IItemProvider>> getTool() {
		return this.tool;
	}

	@Override
	public Optional<BlockState> getBlockState() {
		return this.blockState;
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderType.LOOT_TABLE.get();
	}

}
