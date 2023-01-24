package highfox.inventoryactions.action.condition;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.ActionConditionType;
import highfox.inventoryactions.api.condition.ItemSourcingCondition;
import highfox.inventoryactions.api.itemsource.IItemSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf.Reader;
import net.minecraft.network.FriendlyByteBuf.Writer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.TierSortingRegistry;

public class ItemTierCondition extends ItemSourcingCondition {
	private final Optional<Tier> minimumTier;
	private final Optional<Tier> maximumTier;
	private final Supplier<List<Tier>> allowedTiers;

	public ItemTierCondition(IItemSource source, Optional<Tier> minimumTier, Optional<Tier> maximumTier) {
		super(source);
		this.minimumTier = minimumTier;
		this.maximumTier = maximumTier;
		this.allowedTiers = Suppliers.memoize(this::calculateValidTiers);
	}

	@Override
	public boolean test(IActionContext context) {
		ItemStack stack = this.source.get(context);

		if (stack.getItem() instanceof TieredItem item) {
			return this.allowedTiers.get().contains(item.getTier());
		}

		return false;
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionTypes.ITEM_TIER.get();
	}

	private List<Tier> calculateValidTiers() {
		if (this.minimumTier.isPresent() && this.minimumTier.equals(this.maximumTier)) {
			return ImmutableList.of(this.minimumTier.get());
		}

		Stream<Tier> stream = TierSortingRegistry.getSortedTiers().stream();

		if (this.minimumTier.isPresent()) {
			stream = stream.dropWhile(tier -> tier != this.minimumTier.get());
		}
		if (this.maximumTier.isPresent()) {
			AtomicBoolean keepGoing = new AtomicBoolean(true);
			stream = stream.takeWhile(tier -> tier != this.maximumTier.get() ? keepGoing.get() : keepGoing.getAndSet(false));
		}

		return stream.collect(ImmutableList.toImmutableList());
	}

	@Override
	public void additionalToNetwork(FriendlyByteBuf buffer) {
		Writer<Tier> writer = (buf, tier) -> buf.writeResourceLocation(TierSortingRegistry.getName(tier));
		buffer.writeOptional(this.minimumTier, writer);
		buffer.writeOptional(this.maximumTier, writer);
	}

	public static class Deserializer extends BaseDeserializer<ItemTierCondition> {

		@Override
		public ItemTierCondition fromJson(JsonObject json, JsonDeserializationContext context, IItemSource source) {
			JsonElement tierElement = json.get("tier");

			Optional<Tier> minimumTier;
			Optional<Tier> maximumTier;
			if (GsonHelper.isStringValue(tierElement)) {
				minimumTier = maximumTier = this.getTier(tierElement);
			} else if (tierElement.isJsonObject()) {
				JsonObject object = tierElement.getAsJsonObject();
				minimumTier = this.getTier(object, "minimum");
				maximumTier = this.getTier(object, "maximum");

				if (minimumTier.isEmpty() && maximumTier.isEmpty()) {
					throw new JsonSyntaxException("Tier range must contain at least a minimum or maximum tier");
				} else if (minimumTier.isPresent() && maximumTier.isPresent()) {
					if (TierSortingRegistry.getTiersLowerThan(minimumTier.get()).contains(maximumTier.get())) {
						throw new JsonSyntaxException("Minimum tier must be lower than or equal to maximum tier");
					}
				}
			} else {
				throw new JsonSyntaxException("Expected tier range to be an object or string, was " + GsonHelper.getType(tierElement));
			}

			return new ItemTierCondition(source, minimumTier, maximumTier);
		}

		@Override
		public ItemTierCondition fromNetwork(FriendlyByteBuf buffer, IItemSource source) {
			Reader<Tier> reader = buf -> TierSortingRegistry.byName(buf.readResourceLocation());
			Optional<Tier> minimumTier = buffer.readOptional(reader);
			Optional<Tier> maximumTier = buffer.readOptional(reader);

			return new ItemTierCondition(source, minimumTier, maximumTier);
		}

		private Optional<Tier> getTier(JsonObject json, String memberName) {
			if (!json.has(memberName)) {
				return Optional.empty();
			} else {
				return this.getTier(json.get(memberName));
			}
		}

		private Optional<Tier> getTier(JsonElement element) {
			ResourceLocation tierName = new ResourceLocation(GsonHelper.convertToString(element, "tier"));
			Tier tier = TierSortingRegistry.byName(tierName);

			if (tier != null) {
				return Optional.of(tier);
			} else {
				throw new JsonSyntaxException("Unknown item tier: " + tierName);
			}
		}

	}

}
