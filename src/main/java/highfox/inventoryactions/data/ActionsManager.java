package highfox.inventoryactions.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import highfox.inventoryactions.InventoryActions;
import highfox.inventoryactions.action.ActionContext;
import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.network.message.SyncActionsMessage;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class ActionsManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder())
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
	private static ImmutableMap<ResourceLocation, InventoryAction> ALL_ACTIONS = ImmutableMap.of();
	private final Supplier<RegistryAccess> registryAccess = Suppliers.memoize(RegistryAccess::builtinCopy);

	public ActionsManager() {
		super(GSON, "inventory_actions");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registryAccess.get());
		Map<ResourceLocation, InventoryAction> loadedActions = Maps.newHashMap();

		elements.forEach((id, element) -> {
			JsonObject jsonObject = element.getAsJsonObject();
			boolean remove = GsonHelper.getAsBoolean(jsonObject, "empty", false);
			boolean replace = loadedActions.containsKey(id);
			if (remove || replace) {
				if (!id.getNamespace().equalsIgnoreCase(InventoryActions.MODID)) {
					InventoryActions.LOG.error("Cannot override custom inventory action {}, skipping", id);
					return;
				}

				loadedActions.remove(id);
				if (remove) {
					return;
				}
			}

			DataResult<InventoryAction> result = InventoryAction.CODEC.parse(ops, element);
			result.get().<Optional<InventoryAction>>map(Optional::of, error -> {
				InventoryActions.LOG.error("Error loading inventory action {}: {}", id, error.message());
				return Optional.empty();
			}).ifPresent(action -> {
				loadedActions.put(id, action);
			});
		});

		setActions(ImmutableMap.copyOf(loadedActions));

		int loadedDefaultActions = ALL_ACTIONS.keySet().stream().filter(id -> id.getNamespace().equals(InventoryActions.MODID)).toList().size();
		InventoryActions.LOG.info("Loaded {} inventory actions ({} default, {} custom)", ALL_ACTIONS.size(), loadedDefaultActions, ALL_ACTIONS.size() - loadedDefaultActions);
	}

	public static SyncActionsMessage getSyncMessage() {
		return new SyncActionsMessage(ImmutableMap.copyOf(ALL_ACTIONS));
	}

	public static void setActions(ImmutableMap<ResourceLocation, InventoryAction> map) {
		ALL_ACTIONS = map;
	}

	public static void clearActions() {
		ALL_ACTIONS = ImmutableMap.of();
		InventoryActions.LOG.debug("Cleared inventory actions");
	}

	@Nullable
	public static InventoryAction getAction(ResourceLocation id) {
		return ALL_ACTIONS.getOrDefault(id, null);
	}

	public static Collection<InventoryAction> getAllActions() {
		return Collections.unmodifiableCollection(ALL_ACTIONS.values());
	}

	public static Optional<InventoryAction> getActionForContext(ActionContext context) {
		return getAllActions().stream().filter(action -> action.canRunAction(context)).findFirst();
	}

}
