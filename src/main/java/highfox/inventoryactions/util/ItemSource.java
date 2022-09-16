package highfox.inventoryactions.util;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import highfox.inventoryactions.action.ActionContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ItemSource {
	private static final Map<String, Factory<?>> FACTORIES = ImmutableMap.<String, Factory<?>>builder()
			.put("action_target", str -> ActionTarget.getInstance())
			.put("action_using", str -> ActionUsing.getInstance())
			.put("player", PlayerSource::valueOf)
			.build();

	public static final Codec<ItemSource> CODEC = Codec.STRING.comapFlatMap(str -> {
		str = str.toLowerCase();
		final String separator = ".";
		String name = str.contains(separator) ? str.substring(0, str.indexOf(separator)) : str;
		if (FACTORIES.containsKey(name)) {
			ItemSource source = FACTORIES.get(name).valueOf(str);
			if (source != null) {
				return DataResult.success(source);
			}
		}
		return DataResult.error("Invalid item source: " + str);
	}, ItemSource::toString);

	public abstract ItemStack get(ActionContext context);
	public abstract void setAndUpdate(ActionContext context, ItemStack stack);
	@Override
	public abstract String toString();

	@FunctionalInterface
	protected static interface Factory<T extends ItemSource> {
		@Nullable
		public T valueOf(String str);
	}

	private static class ActionTarget extends ItemSource {
		private static final ActionTarget INSTANCE = new ActionTarget();
		private final String name = "action_target";

		private ActionTarget() {
		}

		public static ActionTarget getInstance() {
			return INSTANCE;
		}

		@Override
		public ItemStack get(ActionContext context) {
			return context.getTarget();
		}

		@Override
		public void setAndUpdate(ActionContext context, ItemStack stack) {
			context.getSlot().set(stack);
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private static class ActionUsing extends ItemSource {
		private static final ActionUsing INSTANCE = new ActionUsing();
		private final String name = "action_using";

		private ActionUsing() {
		}

		public static ActionUsing getInstance() {
			return INSTANCE;
		}

		@Override
		public ItemStack get(ActionContext context) {
			return context.getUsing();
		}

		@Override
		public void setAndUpdate(ActionContext context, ItemStack stack) {
			context.getPlayer().containerMenu.setCarried(stack);
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public static class PlayerSource extends ItemSource {
		// Partially taken from SlotArgument
		private static final BiMap<String, Integer> SLOT_BY_NAME = Util.make(new ImmutableBiMap.Builder<String, Integer>(), builder -> {
			for (int i = 0; i < 9; i++) {
				builder.put("player.hotbar." + i, i);
			}

			for(int i = 0; i < 27; ++i) {
				builder.put("player.inventory." + i, 9 + i);
			}

			builder.put("player.weapon.mainhand", EquipmentSlot.MAINHAND.getIndex(98));
			builder.put("player.weapon.offhand", EquipmentSlot.OFFHAND.getIndex(98));
			builder.put("player.armor.head", EquipmentSlot.HEAD.getIndex(100));
			builder.put("player.armor.chest", EquipmentSlot.CHEST.getIndex(100));
			builder.put("player.armor.legs", EquipmentSlot.LEGS.getIndex(100));
			builder.put("player.armor.feet", EquipmentSlot.FEET.getIndex(100));
		}).build();
		private static final Supplier<Map<Integer, String>> NAME_BY_SLOT = Suppliers.memoize(SLOT_BY_NAME::inverse);
		private static final Int2ObjectMap<PlayerSource> SOURCES = new Int2ObjectArrayMap<>();
		private final int slot;

		private PlayerSource(int slot) {
			this.slot = slot;
		}

		public static PlayerSource valueOf(String str) {
			if (!SLOT_BY_NAME.containsKey(str)) {
				return null;
			} else {
				return SOURCES.computeIfAbsent(SLOT_BY_NAME.get(str), PlayerSource::new);
			}
		}

		@Override
		public ItemStack get(ActionContext context) {
			Player player = context.getPlayer();
			SlotAccess access = player.getSlot(this.slot);

			if (access != SlotAccess.NULL) {
				return access.get();
			}

			return ItemStack.EMPTY;
		}

		@Override
		public void setAndUpdate(ActionContext context, ItemStack stack) {
			Player player = context.getPlayer();
			SlotAccess access = context.getPlayer().getSlot(this.slot);
			if (access != SlotAccess.NULL) {
				access.set(stack);
				player.getInventory().setChanged();
				player.inventoryMenu.broadcastChanges();
			}
		}

		@Override
		public String toString() {
			Map<Integer, String> names = NAME_BY_SLOT.get();
			if (!names.containsKey(this.slot)) {
				throw new IllegalStateException("Unknown slot id: " + this.slot);
			}

			return names.get(this.slot);
		}

	}

}
