package hu.tryharddood.myzone.MenuBuilder.inventory;

import org.bukkit.inventory.Inventory;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;
import org.inventivetalent.reflection.util.AccessUtil;

public class InventoryHelper
{

	static Class<?> obcCraftInventory;
	static Class<?> obcCraftInventoryCustom;
	static Class<?> obcMinecraftInventory;

	static {
		try {
			obcCraftInventory = new OBCClassResolver().resolveSilent("inventory.CraftInventory");
			obcCraftInventoryCustom = new OBCClassResolver().resolveSilent("inventory.CraftInventoryCustom");
			for (Class<?> c : obcCraftInventoryCustom.getDeclaredClasses()) {
				if (c.getSimpleName().equals("MinecraftInventory")) {
					obcMinecraftInventory = c;
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void changeTitle(Inventory inv, String title)
	{
		try {
			Object minecrafInventory = AccessUtil.setAccessible(obcCraftInventory.getDeclaredField("inventory")).get(inv);
			AccessUtil.setAccessible(obcMinecraftInventory.getDeclaredField("title")).set(minecrafInventory, title);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
