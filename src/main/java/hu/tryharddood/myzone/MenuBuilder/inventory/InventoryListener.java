package hu.tryharddood.myzone.MenuBuilder.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryListener implements Listener
{

	public static final Map<Inventory, InventoryMenuBuilder> builderMap = new HashMap<>();
	private final Map<Inventory, Map<ClickType, List<InventoryMenuListener>>> listenerMap     = new HashMap<>();
	private final Map<Inventory, List<InventoryEventHandler>>                 eventHandlerMap = new HashMap<>();

	public void registerListener(InventoryMenuBuilder builder, InventoryMenuListener listener, ClickType[] actions)
	{
		Map<ClickType, List<InventoryMenuListener>> map = listenerMap.get(builder.getInventory());
		if (map == null) {
			map = new HashMap<>();
		}
		for (ClickType action : actions) {
			List<InventoryMenuListener> list = map.get(action);
			if (list == null) {
				list = new ArrayList<>();
			}
			if (list.contains(listener)) {
				throw new IllegalArgumentException("listener already registered");
			}
			list.add(listener);

			map.put(action, list);
		}

		listenerMap.put(builder.getInventory(), map);
	}

	public void unregisterListener(InventoryMenuBuilder builder, InventoryMenuListener listener, ClickType[] actions)
	{
		Map<ClickType, List<InventoryMenuListener>> map = listenerMap.get(builder.getInventory());
		if (map == null) {
			return;
		}
		for (ClickType action : actions) {
			List<InventoryMenuListener> list = map.get(action);
			if (list == null) {
				continue;
			}
			list.remove(listener);
		}
	}

	public void unregisterAllListeners(Inventory inventory)
	{
		listenerMap.remove(inventory);
	}

	public void registerEventHandler(InventoryMenuBuilder builder, InventoryEventHandler eventHandler)
	{
		List<InventoryEventHandler> list = eventHandlerMap.get(builder.getInventory());
		if (list == null) {
			list = new ArrayList<>();
		}
		if (!list.contains(eventHandler)) {
			list.add(eventHandler);
		}

		eventHandlerMap.put(builder.getInventory(), list);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event)
	{
		Player    player    = (Player) event.getWhoClicked();
		Inventory inventory = event.getInventory(); // <- Fix for moving items from the player's inventory to the gui
		ClickType type      = event.getClick();

		if (listenerMap.containsKey(inventory)) {
			event.setCancelled(true);
			event.setResult(Event.Result.DENY);

			if(event.getRawSlot() >= event.getInventory().getSize()){
				return;
			}

			if (event.getCurrentItem() != null && event.getClickedInventory().contains(event.getCurrentItem())) { // <- Fix for moving items from the player's inventory to the gui

				Map<ClickType, List<InventoryMenuListener>> actionMap = listenerMap.get(inventory);
				if (actionMap.containsKey(type)) {
					List<InventoryMenuListener> listeners = actionMap.get(type);

					for (InventoryMenuListener listener : listeners) {

						try {
							InventoryMenuBuilder builder = getBuilderForInventory(inventory);
							if (builder instanceof PageInventoryBuilder) {
								ItemStack            item        = inventory.getItem(event.getSlot());
								PageInventoryBuilder pageBuilder = (PageInventoryBuilder) builder;

								if (pageBuilder.getExitInventory() != null) {
									if (item.equals(pageBuilder.getExitInventory())) {
										player.closeInventory();
									}
								}

								int newPage = 0;
								if (item.equals(pageBuilder.getBackPage())) {
									newPage = -1;
								}
								else if (item.equals(pageBuilder.getForwardsPage())) {
									newPage = 1;
								}
								if (newPage != 0) {
									pageBuilder.setPage(pageBuilder.getCurrentPage() + newPage);
								}
							}

							listener.interact(player, type, event);
						}
						catch (Throwable throwable) {
							throwable.printStackTrace();
						}
					}
				}
			}
		}

		if (eventHandlerMap.containsKey(inventory)) {
			List<InventoryEventHandler> list = eventHandlerMap.get(inventory);

			for (InventoryEventHandler handler : list) {
				try {
					handler.handle(event);
				}
				catch (Throwable throwable) {
					throwable.printStackTrace();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryCloseEvent(InventoryCloseEvent event)
	{
		Inventory inventory = event.getInventory();

		if (listenerMap.containsKey(inventory)) {
			listenerMap.remove(inventory);
		}
		if (eventHandlerMap.containsKey(inventory)) {
			eventHandlerMap.remove(inventory);
		}
		if (builderMap.containsKey(inventory)) {
			builderMap.remove(inventory);
		}
	}

	private InventoryMenuBuilder getBuilderForInventory(Inventory inventory)
	{
		for (Map.Entry<Inventory, InventoryMenuBuilder> entry : builderMap.entrySet()) {
			if (entry.getKey().getTitle().equals(inventory.getTitle())) {
				return entry.getValue();
			}
		}
		return null;
	}
}
