package highfox.inventoryactions.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public interface LootContextParamSetsInvoker {
	@Invoker
	public static LootContextParamSet invokeRegister(String name, Consumer<LootContextParamSet.Builder> builderConsumer) {
		throw new AssertionError();
	}
}
