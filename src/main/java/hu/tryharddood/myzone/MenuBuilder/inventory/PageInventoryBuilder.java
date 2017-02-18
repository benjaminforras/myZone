package hu.tryharddood.myzone.MenuBuilder.inventory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;

public class PageInventoryBuilder extends InventoryMenuBuilder
{
	private static int inventorySize;
	private final HashMap<Integer, ItemStack[]> pages = new HashMap<>();
	private ItemStack backAPage;
	private ItemStack forwardsAPage;
	private ItemStack exitInventory;
	private        int currentPage;

	public PageInventoryBuilder(String inventoryName, ArrayList<ItemStack> itemStacks)
	{
		super(getInventorySize(itemStacks.size()), inventoryName);
		setPages(itemStacks.toArray(new ItemStack[itemStacks.size()]));
	}

	public PageInventoryBuilder(String inventoryName, int inventorySize)
	{
		super(inventorySize, inventoryName);
	}

	private static int getInventorySize(int size)
	{
		inventorySize = size == 0 ? 9 : size > 54 ? 54 : (int) Math.min(54, Math.ceil((double) size / 9) * 9);
		return inventorySize;
	}

	public void setItems(ArrayList<ItemStack> itemStacks)
	{
		setPages(itemStacks.toArray(new ItemStack[itemStacks.size()]));
	}

	public ItemStack getBackPage()
	{
		if (backAPage == null) {
			backAPage = new ItemBuilder(Material.PAPER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + tl("GUI_Back")).addLore(tl("GUI_NextLore")).build();
		}
		return backAPage;
	}

	public void setBackPage(ItemStack itemStack)
	{
		backAPage = itemStack;
	}

	public int getCurrentPage()
	{
		return currentPage;
	}

	public ItemStack getForwardsPage()
	{
		if (forwardsAPage == null) {
			forwardsAPage = new ItemBuilder(Material.PAPER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + tl("GUI_Next")).addLore(tl("GUI_BackLore")).build();
		}
		return forwardsAPage;
	}

	public void setForwardsAPage(ItemStack itemStack)
	{
		forwardsAPage = itemStack;
	}

	public ItemStack getExitInventory()
	{
		return exitInventory;
	}

	public void setExitInventory(ItemStack item)
	{
		this.exitInventory = item;
	}

	public void setPage(int newPage)
	{
		if (pages.containsKey(newPage)) {
			currentPage = newPage;
			getInventory().clear();

			ItemStack[] pageItems = getItemsForPage();
			withItems(pageItems);
		}
	}

	private ItemStack[] getItemsForPage()
	{
		ItemStack[] pageItems = pages.get(Math.max(getCurrentPage(), 0));
		int         pageSize  = getInventory().getSize();
		pageItems = Arrays.copyOf(pageItems, pageSize);
		if (getCurrentPage() > 0 || getExitInventory() != null) {
			pageItems[pageItems.length - 9] = getCurrentPage() == 0 ? getExitInventory() : getBackPage();
		}
		if (pages.size() - 1 > getCurrentPage()) {
			pageItems[pageItems.length - 1] = getForwardsPage();
		}
		return pageItems;
	}

	private void setPages(ItemStack... allItems)
	{
		pages.clear();
		int         invPage     = 0;
		boolean     usePages    = getExitInventory() != null || allItems.length > inventorySize;
		ItemStack[] items       = null;
		int         currentSlot = 0;
		for (int currentItem = 0; currentItem < allItems.length; currentItem++) {
			if (items == null) {
				int newSize = allItems.length - currentItem;
				if (usePages && newSize + 9 > inventorySize) {
					newSize = inventorySize - 9;
				}
				else if (newSize > inventorySize) {
					newSize = inventorySize;
				}
				items = new ItemStack[newSize];
			}
			ItemStack item = allItems[currentItem];
			items[currentSlot++] = item;
			if (currentSlot == items.length) {
				pages.put(invPage, items);
				invPage++;
				currentSlot = 0;
				items = null;
			}
		}
		if (pages.keySet().size() < getCurrentPage()) {
			currentPage = pages.keySet().size() - 1;
		}
		if (allItems.length == 0) {
			int itemsSize = (int) (Math.ceil((double) inventorySize / 9)) * 9;

			items = new ItemStack[Math.min(54, itemsSize)];
			pages.put(0, items);
		}
		setPage(getCurrentPage());
	}
}
