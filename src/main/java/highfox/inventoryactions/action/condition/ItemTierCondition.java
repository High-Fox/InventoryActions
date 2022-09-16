package highfox.inventoryactions.action.condition;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.TierSortingRegistry;

public class ItemTierCondition extends ItemSourcingCondition {
	public static final Codec<ItemTierCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(
			UtilCodecs.rangedCodec(ResourceLocation.CODEC, "minimum", "maximum", (min, max) -> {
				return DataResult.success(Pair.of(min, max));
			}, pair -> pair.getFirst().orElse(null), pair -> pair.getSecond().orElse(null)).fieldOf("tier").forGetter(o -> o.tierRange)
	).apply(instance, ItemTierCondition::new));

	private final Pair<Optional<ResourceLocation>, Optional<ResourceLocation>> tierRange;
	private List<Tier> allowedTiers;

	public ItemTierCondition(ItemSource source, Pair<Optional<ResourceLocation>, Optional<ResourceLocation>> tierRange) {
		super(source);
		this.tierRange = tierRange;
		this.allowedTiers = ImmutableList.of();
	}

	@Override
	public boolean test(ActionContext context) {
		ItemStack stack = this.source.get(context);

		if (stack.getItem() instanceof TieredItem item) {
			if (this.allowedTiers.isEmpty()) {
				Optional<ResourceLocation> minimum = this.tierRange.getFirst();
				Optional<ResourceLocation> maximum = this.tierRange.getSecond();
				Optional<Tier> minimumTier = minimum.map(TierSortingRegistry::byName);
				Optional<Tier> maximumTier = maximum.map(TierSortingRegistry::byName);
				if ((minimum.isPresent() && minimumTier.isEmpty()) || (maximum.isPresent() && maximumTier.isEmpty())) {
					InventoryActions.LOG.warn("Unknown item tier: {}", minimumTier.isEmpty() ? minimum.get() : maximum.get());
				}

				this.allowedTiers = this.getTiersBetween(minimumTier, maximumTier);
			}

			return this.allowedTiers.contains(item.getTier());
		}

		return false;
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ITEM_TIER.get();
	}

	private List<Tier> getTiersBetween(Optional<Tier> min, Optional<Tier> max) {
		if (min.isPresent() && min.equals(max)) {
			return ImmutableList.of(min.get());
		}

		Stream<Tier> stream = TierSortingRegistry.getSortedTiers().stream();

		if (min.isPresent()) {
			stream = stream.dropWhile(tier -> tier != min.get());
		}
		if (max.isPresent()) {
			AtomicBoolean keepGoing = new AtomicBoolean(true);
			stream = stream.takeWhile(tier -> tier != max.get() ? keepGoing.get() : keepGoing.getAndSet(false));
		}

		return stream.collect(ImmutableList.toImmutableList());
	}

}
