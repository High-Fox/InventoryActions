package highfox.inventoryactions.action.function.provider;

import java.util.Optional;
import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public class TagItemProvider extends ItemFunctionsProvider implements ISingleItemResult {
	public static final Codec<TagItemProvider> CODEC = RecordCodecBuilder.create(instance -> functionsCodec(instance).and(instance.group(
			TagKey.codec(ForgeRegistries.Keys.ITEMS).fieldOf("tag").forGetter(o -> o.tagKey),
			UtilCodecs.optionalFieldOf(UtilCodecs.NUMBER_PROVIDER_CODEC, "amount").forGetter(o -> o.amount)
	)).apply(instance, TagItemProvider::new));
	private final TagKey<Item> tagKey;
	private final Optional<NumberProvider> amount;
	private final ObjectArrayList<Item> tagContents;

	public TagItemProvider(LootItemFunction[] modifiers, Optional<Either<ItemSource, IItemProvider>> tool, Optional<BlockState> blockState, TagKey<Item> tagKey, Optional<NumberProvider> amount) {
		super(modifiers, tool, blockState);
		this.tagKey = tagKey;
		this.amount = amount;
		this.tagContents = this.getTag().stream().collect(ObjectArrayList.toList());
	}

	@Override
	public void addItems(ActionContext context, Random random, ObjectArrayList<ItemStack> results) {
		if (this.amount.isPresent()) {
			int times = this.amount.get().getInt(context.getLootContext());

			for (int i = 0; i < times; i++) {
				results.add(this.getItem(context, random));
			}
		} else {
			this.tagContents.stream().map(ItemStack::new).forEach(stack -> results.add(this.applyModifiers(context, stack)));
		}
	}

	@Override
	public ItemStack getItem(ActionContext context, Random random) {
		return Util.getRandomSafe(this.tagContents, random).map(item -> {
			ItemStack result = new ItemStack(item);
			return this.applyModifiers(context, result);
		}).orElse(ItemStack.EMPTY);
	}

	private ITag<Item> getTag() {
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		return tags.getTag(this.tagKey);
	}

	@Override
	public ItemProviderType getType() {
		return ItemProviderType.TAG.get();
	}

}
