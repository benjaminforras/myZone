package hu.tryharddood.myzone.MenuBuilder.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Listener for Inventory menu interactions
 */
public interface InventoryMenuListener
{

	/**
	 * Called when a player clicks the inventory
	 *
	 * @param player {@link Player} who clicked
	 * @param action the {@link ClickType} performed
	 * @param event   the event
	 */
	void interact(Player player, ClickType action, InventoryClickEvent event);

}
