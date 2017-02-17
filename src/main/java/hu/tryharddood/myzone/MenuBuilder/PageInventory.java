package hu.tryharddood.myzone.MenuBuilder;

import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;

/*****************************************************
 *              Created by TryHardDood on 2016. 10. 31..
 ****************************************************/

public final class PageInventory extends InventoryMenuBuilder
{

	private final HashMap<Integer, ItemStack[]> pages = new HashMap<>();

	private ItemStack backAPage;
	private ItemStack forwardsAPage;
	private ItemStack paddingItem;

	private int currentPage;

	public PageInventory(String inventoryName, ArrayList<ItemStack> itemStacks)
	{
		super(getInventorySize(itemStacks.size()), inventoryName);
		setPages(itemStacks);
	}

	private static int getInventorySize(int size)
	{
		return size == 0 ? 9 : size > 54 ? 54 : (int) Math.min(54, Math.ceil((double) size / 9) * 9);
	}

	public ItemStack getBackPage()
	{
		if (backAPage == null) {
			backAPage = new ItemBuilder(Material.PAPER).setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + tl("GUI_Back")).addLore(tl("GUI_NextLore")).build();
		}
		return backAPage;
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

	private ItemStack[] getItemsForPage()
	{
		/*ItemStack[] pageItems = pages.get(Math.max(getCurrentPage(), 0));
		int         pageSize  = pageItems.length;
		if (pages.size() > 1)
		{
			pageSize += 9;
		}
		pageItems = Arrays.copyOf(pageItems, pageSize);
		if (getCurrentPage() > 0)
		{
			pageItems[pageItems.length - 9] = getBackPage();
		}

		if (pages.size() - 1 > getCurrentPage())
		{
			pageItems[pageItems.length - 1] = getForwardsPage();
		}

		if (getCurrentPage() > 0 || pages.size() - 1 > getCurrentPage())
		{
			if (pageItems[pageItems.length - 9] == null)
			{
				pageItems[pageItems.length - 9] = getPaddingItem();
			}

			if (pageItems[pageItems.length - 1] == null)
			{
				pageItems[pageItems.length - 1] = getPaddingItem();
			}

			pageItems[pageItems.length - 8] = getPaddingItem();
			pageItems[pageItems.length - 7] = getPaddingItem();
			pageItems[pageItems.length - 6] = getPaddingItem();
			pageItems[pageItems.length - 5] = getPaddingItem();
			pageItems[pageItems.length - 4] = getPaddingItem();
			pageItems[pageItems.length - 3] = getPaddingItem();
			pageItems[pageItems.length - 2] = getPaddingItem();
		}

		return pageItems;*/

		ItemStack[] pageItems = pages.get(Math.max(getCurrentPage(), 0));
		int         pageSize  = pageItems.length;
		if (pages.size() > 1) {
			pageSize += 9;
		}
		pageSize = ((pageSize + 8) / 9) * 9;

		pageItems = Arrays.copyOf(pageItems, pageSize);
		if (getCurrentPage() > 0) {
			pageItems[pageItems.length - 9] = getCurrentPage() == 0 ? new ItemStack(Material.AIR) : getBackPage();
		}
		if (pages.size() - 1 > getCurrentPage()) {
			pageItems[pageItems.length - 1] = getForwardsPage();
		}
		return pageItems;
	}

	private void setPages(ItemStack... allItems)
	{
		pages.clear();
		int         invPage       = 0;
		int         inventorySize = 54;
		boolean     usePages      = allItems.length > inventorySize;
		ItemStack[] items         = null;
		int         currentSlot   = 0;
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

	private void setPages(ArrayList<ItemStack> allItems)
	{
		setPages(allItems.toArray(new ItemStack[allItems.size()]));
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

	private ItemStack getPaddingItem()
	{
		if (paddingItem == null) {
			paddingItem = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 0).setTitle("§8").build();
		}
		return paddingItem;
	}
}

