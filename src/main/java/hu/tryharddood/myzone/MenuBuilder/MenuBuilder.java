package hu.tryharddood.myzone.MenuBuilder;

import org.bukkit.entity.HumanEntity;

/**
 * Core MenuBuilder class
 * Use {@link hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder} or {@link hu.tryharddood.myzone.MenuBuilder.chat.ChatMenuBuilder}
 *
 * @param <T> Type of the builder
 * @see hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder
 * @see hu.tryharddood.myzone.MenuBuilder.chat.ChatMenuBuilder
 */
public abstract class MenuBuilder<T>
{

	public MenuBuilder()
	{
	}

	/**
	 * Shows the Menu to the viewers
	 */
	public abstract MenuBuilder show(HumanEntity... viewers);

	/**
	 * Refreshes the content of the menu
	 */
	public abstract MenuBuilder refreshContent();

	/**
	 * Builds the menu
	 */
	public abstract <T> T build();

	public abstract void dispose();
}
