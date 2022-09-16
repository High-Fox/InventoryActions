package highfox.inventoryactions.action.function;

import java.util.Queue;

import com.mojang.serialization.Codec;

import highfox.inventoryactions.action.ActionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public interface IActionFunction {
	public static final Codec<IActionFunction> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ActionFunctionType.FUNCTION_TYPES.get().getCodec()).dispatch(IActionFunction::getType, ActionFunctionType::getCodec);

	void run(Queue<Runnable> workQueue, ActionContext context);
	ActionFunctionType getType();

	default ResourceLocation getRegistryName() {
		return ActionFunctionType.FUNCTION_TYPES.get().getKey(this.getType());
	}

}
