package highfox.inventoryactions.action.condition;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.ItemSource;
import highfox.inventoryactions.util.UtilCodecs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemCondition extends ItemSourcingCondition {
	public static final Codec<ItemCondition> CODEC = RecordCodecBuilder.create(instance -> sourceCodec(instance).and(instance.group(
			UtilCodecs.optionalFieldOf(RegistryCodecs.homogeneousList(ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS.getCodec()), "items").forGetter(o -> o.items),
			UtilCodecs.optionalFieldOf(UtilCodecs.PATTERN, "namespace").forGetter(o -> o.namespacePattern),
			UtilCodecs.optionalFieldOf(UtilCodecs.PATTERN, "path").forGetter(o -> o.pathPattern)
	)).apply(instance, ItemCondition::new));

	private final Optional<HolderSet<Item>> items;
	private final Optional<Pattern> namespacePattern;
	private final Predicate<String> namespacePredicate;
	private final Optional<Pattern> pathPattern;
	private final Predicate<String> pathPredicate;

	public ItemCondition(ItemSource source, Optional<HolderSet<Item>> items, Optional<Pattern> namespacePattern, Optional<Pattern> pathPattern) {
		super(source);
		this.items = items;
		this.namespacePattern = namespacePattern;
		this.namespacePredicate = this.namespacePattern.map(Pattern::asPredicate).orElse(str -> true);
		this.pathPattern = pathPattern;
		this.pathPredicate = this.pathPattern.map(Pattern::asPredicate).orElse(str -> true);
	}

	@Override
	public boolean test(ActionContext context) {
		ItemStack stack = this.source.get(context);
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());

		if (this.items.isPresent() && !this.items.get().contains(stack.getItem().builtInRegistryHolder())) {
			return false;
		} else if (!this.namespacePredicate.test(id.getNamespace()) || !this.pathPredicate.test(id.getPath())) {
			return false;
		}

		return true;
	}

	@Override
	public ActionConditionType getType() {
		return ActionConditionType.ITEM.get();
	}

}
