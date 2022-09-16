package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.util.UtilCodecs;
import highfox.inventoryactions.util.UtilCodecs.SourceOrItem;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CraftingFunction implements IActionFunction {
	public static final Codec<CraftingFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().flatXmap(checkPattern(), checkPattern()).fieldOf("pattern").forGetter(o -> o.pattern),
			Codec.unboundedMap(Codec.STRING, UtilCodecs.SOURCE_OR_ITEM_CODEC).flatXmap(checkKey(), checkKey()).fieldOf("key").forGetter(o -> o.key)
	).apply(instance, CraftingFunction::new));

	private final List<String> pattern;
	private final Map<String, SourceOrItem> key;

	public CraftingFunction(List<String> pattern, Map<String, SourceOrItem> key) {
		this.pattern = pattern;
		this.key = key;
	}

	@Override
	public void run(Queue<Runnable> workQueue, ActionContext context) {
		int width = this.pattern.get(0).length();
		int height = this.pattern.size();
		CraftingContainer container = this.makeCraftingContainer(width, height);
		Set<String> unusedKeys = Sets.newHashSet(this.key.keySet());

		for (int i = 0; i < this.pattern.size(); i++) {
			for (int j = 0; j < this.pattern.get(i).length(); j++) {
				String s = this.pattern.get(i).substring(j, j + 1);

				if (!s.isBlank()) {
					SourceOrItem value = this.key.get(s);

					if (value == null) {
						throw new IllegalArgumentException("Pattern references symbol '" + s + "' but it's not defined in the key");
					}

					unusedKeys.remove(s);
					container.setItem(j + width * i, value.get(context));
				}
			}
		}

		Player player = context.getPlayer();
		Level level = player.getLevel();
		Optional<ItemStack> result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level).map(recipe -> recipe.assemble(container));
		if (result.isEmpty()) {
			throw new IllegalArgumentException("Input items do not have a crafting recipe result");
		}

		workQueue.add(() -> {
			result.ifPresent(stack -> {
				GiveItemsFunction.giveItem(stack, context);
				player.awardStat(Stats.ITEM_CRAFTED.get(stack.getItem()), stack.getCount());
			});
		});
	}

	private CraftingContainer makeCraftingContainer(int width, int height) {
		CraftingContainer container = new CraftingContainer(new AbstractContainerMenu((MenuType<?>)null, -1) {
	         @Override
			public ItemStack quickMoveStack(Player p_218264_, int p_218265_) {
	            return ItemStack.EMPTY;
	         }

	         @Override
			public boolean stillValid(Player p_29888_) {
	            return false;
	         }
	      }, width, height);

		return container;
	}

	private static Function<List<String>, DataResult<List<String>>> checkPattern() {
		return list -> {
			if (list.isEmpty()) {
				return DataResult.error("Crafting pattern must not be empty");
			} else if (list.size() > 3) {
				return DataResult.error("Crafting pattern must contain no more than 3 string elements");
			} else if (list.stream().anyMatch(str -> str.length() > 3)) {
				return DataResult.error("Crafting pattern strings must not be longer than 3 characters");
			} else {
				return DataResult.success(list);
			}
		};
	}

	private static Function<Map<String, SourceOrItem>, DataResult<Map<String, SourceOrItem>>> checkKey() {
		return map -> {
			if (map.keySet().stream().anyMatch(key -> key.length() > 1)) {
				return DataResult.error("Crafting pattern keys can only be 1 character");
			} else {
				return DataResult.success(map);
			}
		};
	}

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionType.CRAFTING.get();
	}
}
