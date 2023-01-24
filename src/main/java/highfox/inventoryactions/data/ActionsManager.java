package highfox.inventoryactions.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import highfox.inventoryactions.action.InventoryAction;
import highfox.inventoryactions.action.condition.ActionConditionTypes;
import highfox.inventoryactions.action.function.ActionFunctionTypes;
import highfox.inventoryactions.action.function.provider.ItemProviderTypes;
import highfox.inventoryactions.api.action.IActionContext;
import highfox.inventoryactions.api.condition.IActionCondition;
import highfox.inventoryactions.api.function.IActionFunction;
import highfox.inventoryactions.api.itemmap.ItemMap;
import highfox.inventoryactions.api.itemprovider.IItemProvider;
import highfox.inventoryactions.api.util.ActionsConstants;
import highfox.inventoryactions.network.message.SyncActionsMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.common.util.JsonUtils;

public class ActionsManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = Deserializers.createFunctionSerializer()
			.registerTypeHierarchyAdapter(IActionCondition.class, ActionConditionTypes.createTypeAdapater())
			.registerTypeHierarchyAdapter(IActionFunction.class, ActionFunctionTypes.createTypeAdapater())
			.registerTypeHierarchyAdapter(IItemProvider.class, ItemProviderTypes.createTypeAdapter())
			.registerTypeAdapter(ItemMap.class, new ItemMap.Deserializer())
			.registerTypeAdapter(ImmutableList.class, JsonUtils.ImmutableListTypeAdapter.INSTANCE)
			.registerTypeAdapter(InventoryAction.class, new InventoryAction.Deserializer())
			.create();
	private static ImmutableMap<ResourceLocation, InventoryAction> ALL_ACTIONS = ImmutableMap.of();

	public ActionsManager() {
		super(GSON, "inventory_actions");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, InventoryAction> loadedActions = Maps.newHashMap();

		elements.forEach((id, element) -> {
			JsonObject jsonObject = element.getAsJsonObject();
			boolean remove = GsonHelper.getAsBoolean(jsonObject, "empty", false);
			boolean replace = loadedActions.containsKey(id);
			if (remove || replace) {
				if (!id.getNamespace().equalsIgnoreCase(ActionsConstants.MODID)) {
					ActionsConstants.LOG.error("Cannot override custom inventory action {}, skipping", id);
					return;
				}

				loadedActions.remove(id);
				if (remove) {
					return;
				}
			}

			try {
				InventoryAction action = GSON.fromJson(element, InventoryAction.class);
				loadedActions.put(id, action);
			} catch (JsonParseException | IllegalArgumentException exception) {
				ActionsConstants.LOG.error("Error parsing inventory action {}, skipping", id, exception);
			}
		});

		setActions(ImmutableMap.copyOf(loadedActions));

		int loadedDefaultActions = ALL_ACTIONS.keySet().stream().filter(id -> id.getNamespace().equals(ActionsConstants.MODID)).toList().size();
		ActionsConstants.LOG.info("Loaded {} inventory actions ({} default, {} custom)", ALL_ACTIONS.size(), loadedDefaultActions, ALL_ACTIONS.size() - loadedDefaultActions);
	}

	public static SyncActionsMessage getSyncMessage() {
		return new SyncActionsMessage(ImmutableMap.copyOf(ALL_ACTIONS));
	}

	public static void setActions(ImmutableMap<ResourceLocation, InventoryAction> map) {
		ALL_ACTIONS = map;
	}

	public static void clearActions() {
		ALL_ACTIONS = ImmutableMap.of();
		ActionsConstants.LOG.info("Cleared inventory actions");
	}

	@Nullable
	public static InventoryAction getAction(ResourceLocation id) {
		return ALL_ACTIONS.getOrDefault(id, null);
	}

	public static Collection<InventoryAction> getAllActions() {
		return Collections.unmodifiableCollection(ALL_ACTIONS.values());
	}

	public static Optional<InventoryAction> getActionForContext(IActionContext context) {
		return getAllActions().stream().filter(action -> action.canRunAction(context)).findFirst();
	}

}
