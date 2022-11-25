package highfox.inventoryactions.action.function.provider;

import java.util.Random;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.action.ActionContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public interface IItemProvider {
	public static final Codec<IItemProvider> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ItemProviderType.PROVIDER_TYPES.get().getCodec()).dispatch(IItemProvider::getType, ItemProviderType::getCodec);

	void addItems(ActionContext context, Random random, ObjectArrayList<ItemStack> results);
	ItemProviderType getType();

	default ResourceLocation getRegistryName() {
		return ItemProviderType.PROVIDER_TYPES.get().getKey(this.getType());
	}
}
