package highfox.inventoryactions.action.function;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.function.ActionFunctionType;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.itemsource.SourceOrItem;
import highfox.inventoryactions.api.serialization.IDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.stats.Stats;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CraftingFunction implements IActionFunction {
	private final List<String> pattern;
	private final Map<String, SourceOrItem> key;

	public CraftingFunction(List<String> pattern, Map<String, SourceOrItem> key) {
		this.pattern = pattern;
		this.key = key;
	}

	@Override
	public void run(Queue<Runnable> workQueue, IActionContext context) {
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
					container.setItem(j + width * i, value.resolve(context));
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

	@Override
	public ActionFunctionType getType() {
		return ActionFunctionTypes.CRAFTING.get();
	}

	public static class Deserializer implements IDeserializer<CraftingFunction> {

		@Override
		public CraftingFunction fromJson(JsonObject json, JsonDeserializationContext context) {
			JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
			ImmutableList.Builder<String> patternBuilder = ImmutableList.builderWithExpectedSize(patternArray.size());
			for (JsonElement element : patternArray) {
				String patternElement = GsonHelper.convertToString(element, "pattern string");

				if (patternElement.length() > 3) {
					throw new JsonSyntaxException("Pattern string must be no more than 3 characters");
				} else {
					patternBuilder.add(patternElement);
				}
			}
			List<String> pattern = patternBuilder.build();
			if (pattern.isEmpty()) {
				throw new JsonSyntaxException("Pattern must not be empty");
			} else if (pattern.size() > 3) {
				throw new JsonSyntaxException("Pattern must contain 3 or less strings");
			}
			JsonObject keyObject = GsonHelper.getAsJsonObject(json, "key");
			ImmutableMap.Builder<String, SourceOrItem> keyBuilder = ImmutableMap.builderWithExpectedSize(keyObject.size());
			for (Entry<String, JsonElement> entry : keyObject.entrySet()) {
				String character = entry.getKey();
				if (character.length() != 1 || character.isBlank()) {
					throw new JsonSyntaxException("Key must contain exactly 1 character");
				}

				SourceOrItem sourceOrItem = SourceOrItem.fromJson(entry.getValue());
				keyBuilder.put(character, sourceOrItem);
			}

			return new CraftingFunction(pattern, keyBuilder.build());
		}

		@Override
		public CraftingFunction fromNetwork(FriendlyByteBuf buffer) {
			throw new UnsupportedOperationException();
		}

	}
}
