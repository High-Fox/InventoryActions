package highfox.inventoryactions.util;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import highfox.inventoryactions.action.ActionContext;
import net.minecraft.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ItemSource {
	private static final BiMap<String, ItemSource> SOURCES = Maps.synchronizedBiMap(Util.make(HashBiMap.create(44), map -> {
		map.put("action_target", ItemSource.ActionTarget.INSTANCE);
		map.put("action_using", ItemSource.ActionUsing.INSTANCE);

		for (int i = 0; i < 9; i++) {
			map.put("player.hotbar." + i, ItemSource.PlayerSource.forSlot(i));
		}

		for (int i = 0; i < 27; i++) {
			map.put("player.inventory." + i, ItemSource.PlayerSource.forSlot(9 + i));
		}

		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.MAINHAND.getIndex(98)));
		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.OFFHAND.getIndex(98)));
		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.HEAD.getIndex(100)));
		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.CHEST.getIndex(100)));
		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.LEGS.getIndex(100)));
		map.put("player.weapon.mainhand", ItemSource.PlayerSource.forSlot(EquipmentSlot.FEET.getIndex(100)));
	}));
	private static final Supplier<BiMap<ItemSource, String>> SOURCE_NAMES = Suppliers.memoize(SOURCES::inverse);

	public static final Codec<ItemSource> CODEC = Codec.STRING.flatXmap(str -> {
		str = str.toLowerCase();
		if (SOURCES.containsKey(str)) {
			ItemSource source = SOURCES.get(str);
			if (source != null) {
				return DataResult.success(source);
			}
		}
		return DataResult.error("Invalid item source: " + str);
	}, source -> {
		if (SOURCE_NAMES.get().containsKey(source)) {
			return DataResult.success(SOURCE_NAMES.get().get(source));
		}
		return DataResult.error("Cannot get name for unregistered item source: " + source);
	});

	public abstract ItemStack get(ActionContext context);
	public abstract void setAndUpdate(ActionContext context, ItemStack stack);

	private static class ActionTarget extends ItemSource {
		protected static final ActionTarget INSTANCE = new ActionTarget();

		private ActionTarget() {
		}

		@Override
		public ItemStack get(ActionContext context) {
			return context.getTarget();
		}

		@Override
		public void setAndUpdate(ActionContext context, ItemStack stack) {
			context.getSlot().set(stack);
		}

	}

	private static class ActionUsing extends ItemSource {
		protected static final ActionUsing INSTANCE = new ActionUsing();

		private ActionUsing() {
		}

		@Override
		public ItemStack get(ActionContext context) {
			return context.getUsing();
		}

		@Override
		public void setAndUpdate(ActionContext context, ItemStack stack) {
			context.getPlayer().containerMenu.setCarried(stack);
		}

	}

	public static class PlayerSource extends ItemSource {
		private final int slot;

		protected PlayerSource(int slot) {
			this.slot = slot;
		}

		public static PlayerSource forSlot(int slot) {
			return new PlayerSource(slot);
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

	}

}
