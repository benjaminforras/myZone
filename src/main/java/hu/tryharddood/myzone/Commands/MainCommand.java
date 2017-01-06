package hu.tryharddood.myzone.Commands;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;
import hu.tryharddood.myzone.MenuBuilder.PageInventory;
import hu.tryharddood.myzone.MenuBuilder.PageLayout;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuListener;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class MainCommand extends Subcommand {

	private static ArrayList<Flag> _flagsCache = new ArrayList<>();
	private ProtectedRegion region;

	private InventoryMenuListener flagSettingListener     = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			ItemStack itemStack = event.getCurrentItem();
			if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) return;

			String itemName = itemStack.getItemMeta().getDisplayName();

			Flag flag = myZone.worldGuardReflection.fuzzyMatchFlag(ChatColor.stripColor(itemName));

			Object value;

			if(action == ClickType.RIGHT)
			{
				Bukkit.dispatchCommand(player, "zone flag " + myZone.zoneManager.getRegion(region.getId()).getZoneName() + " " + flag.getName());
				event.getInventory().setItem(event.getSlot(), new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(ChatColor.GRAY + tl("GUI_FlagNotSet", true)).build());
				player.updateInventory();
				return;
			}

			String prefix = "";
			if (flag instanceof StateFlag)
			{
				value = region.getFlag(flag) == StateFlag.State.ALLOW ? "deny" : "allow";

				if (value == "allow")
				{
					prefix = ChatColor.GREEN + tl("GUI_Allow", true);
				}
				else if (value == "deny")
				{
					prefix = ChatColor.RED + tl("GUI_Deny", true);
				}
				else
				{
					prefix = ChatColor.GRAY + tl("GUI_FlagNotSet", true);
				}
			}
			else if (flag instanceof BooleanFlag)
			{
				value = region.getFlag(flag) == (Object) true ? "false" : "true";

				if (value == "true")
				{
					prefix = ChatColor.GREEN + tl("GUI_Allow", true);
				}
				else if (value == "false")
				{
					prefix = ChatColor.RED + tl("GUI_Deny", true);
				}
				else
				{
					prefix = ChatColor.GRAY + tl("GUI_FlagNotSet", true);
				}
			}
			else
			{
				value = "Unknown";
			}

			Bukkit.dispatchCommand(player, "zone flag " + myZone.zoneManager.getRegion(region.getId()).getZoneName() + " " + flag.getName() + " " + value.toString());

			event.getInventory().setItem(event.getSlot(), new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build());
			player.updateInventory();
		}
	};
	private InventoryMenuListener removeMemberListener    = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			ItemStack itemStack = event.getCurrentItem();
			if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) return;

			String itemName   = itemStack.getItemMeta().getDisplayName();
			String playerName = ChatColor.stripColor(itemName);
			Bukkit.dispatchCommand(player, "zone members " + region.getId() + " remove " + playerName);
		}
	};
	private InventoryMenuListener removeOwnerListener     = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			ItemStack itemStack = event.getCurrentItem();
			if (itemStack == null || !itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) return;

			String itemName   = itemStack.getItemMeta().getDisplayName();
			String playerName = ChatColor.stripColor(itemName);
			Bukkit.dispatchCommand(player, "zone owners " + region.getId() + " remove " + playerName);
		}
	};
	private InventoryMenuListener deleteInventoryListener = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			if (event.getCurrentItem() == null) return;

			if (event.getCurrentItem().getDurability() == (short) 13)
			{
				if (Properties.getEconomyEnabled())
				{
					if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneDeleteMoney()))
					{
						player.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneDeleteMoney()));
						return;
					}
					myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneDeleteMoney());
				}

				player.sendMessage(tl("Success") + " " + tl("DeleteZone_Success", region.getId()));
				myZone.worldGuardHelper.deleteRegion(region);
				myZone.worldGuardHelper.saveAll();
			}
			player.closeInventory();
		}
	};

	private InventoryMenuListener settingsMenuListener = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			if (event.getCurrentItem() == null
			    || event.getCurrentItem().getType() == Material.AIR
			    || !event.getCurrentItem().hasItemMeta()
			    || !event.getCurrentItem().getItemMeta().hasDisplayName())
			{
				event.setCancelled(true);
				return;
			}
			if (region == null) return;

			ItemStack            itemStack  = event.getCurrentItem();
			ArrayList<ItemStack> itemStacks = new ArrayList<>();
			if (itemStack.getType() == Material.SIGN)
			{
				itemStacks.clear();

				String prefix = null;
				for (Flag flag : _flagsCache)
				{
					if (!player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", flag.getName())) && !player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", "*")))
					{
						continue;
					}

					if (itemStacks.contains(new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build()))
					{
						continue;
					}

					if (flag instanceof StateFlag)
					{
						if (region.getFlag(flag) == StateFlag.State.ALLOW)
						{ prefix = ChatColor.GREEN + tl("GUI_Allow", true); }
						else if (region.getFlag(flag) == StateFlag.State.DENY)
						{ prefix = ChatColor.RED + tl("GUI_Deny", true); }
						else
						{ prefix = ChatColor.GRAY + tl("GUI_FlagNotSet", true); }

					}
					else if (flag instanceof BooleanFlag)
					{
						if (region.getFlag(flag) == (Object) true)
						{ prefix = ChatColor.GREEN + tl("GUI_Allow", true); }
						else if (region.getFlag(flag) == (Object) false)
						{ prefix = ChatColor.RED + tl("GUI_Deny", true); }
						else
						{ prefix = ChatColor.GRAY + tl("GUI_FlagNotSet", true); }
					}

					itemStacks.add(new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build());
				}

				PageInventory pageInventory = new PageInventory("Flags", itemStacks);
				pageInventory.show(player);

				pageInventory.onInteract(flagSettingListener, ClickType.LEFT, ClickType.RIGHT);
			}
			else if (itemStack.getType() == Material.SKULL_ITEM)
			{
				if (itemStack.getItemMeta().getDisplayName().contains(tl("GUI_Members", true)))
				{
					if (region.getMembers() == null)
					{
						return;
					}
					List<UUID> members = new ArrayList<>();
					members.addAll(region.getMembers().getUniqueIds());

					itemStacks.clear();
					for (int i = 0; i < members.size(); i++)
					{
						itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GRAY + Bukkit.getOfflinePlayer(members.get(i)).getName()).build());
					}

					PageInventory pageInventory = new PageInventory("Members", itemStacks);
					pageInventory.show(player);

					pageInventory.onInteract(removeMemberListener, ClickType.LEFT);
					player.updateInventory();
				}
				else
				{
					if (region.getOwners() == null)
					{
						return;
					}
					List<UUID> owners = new ArrayList<>();
					owners.addAll(region.getOwners().getUniqueIds());

					itemStacks.clear();
					for (int i = 0; i < owners.size(); i++)
					{
						itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GRAY + Bukkit.getOfflinePlayer(owners.get(i)).getName()).build());
					}

					PageInventory pageInventory = new PageInventory("Owners", itemStacks);
					pageInventory.show(player);

					pageInventory.onInteract(removeOwnerListener, ClickType.LEFT);
					player.updateInventory();
				}
			}
			else if (itemStack.getType() == Material.BARRIER)
			{
				InventoryMenuBuilder imb = new InventoryMenuBuilder(27).withTitle("Are you sure?");
				imb.withItem(11, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + tl("GUI_Confirm", true)).build());
				imb.withItem(15, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + tl("GUI_Cancel", true)).build());
				imb.show(player);
				imb.onInteract(deleteInventoryListener, ClickType.LEFT);
			}
		}
	};
	private InventoryMenuListener mainMenuListener     = new InventoryMenuListener() {
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event) {
			if (event.getCurrentItem() == null
			    || event.getCurrentItem().getType() == Material.AIR
			    || !event.getCurrentItem().hasItemMeta()
			    || !event.getCurrentItem().getItemMeta().hasDisplayName())
			{
				event.setCancelled(true);
				return;
			}

			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

			if (myZone.zoneManager.getRegionID(ChatColor.stripColor(itemName)) != null)
			{
				if (myZone.worldGuardHelper.getPlayerMemberRegions(player.getUniqueId()).contains(ChatColor.stripColor(itemName)))
				{
					return;
				}
				region = myZone.worldGuardHelper.getRegion(myZone.zoneManager.getRegionID(ChatColor.stripColor(itemName)));

				InventoryMenuBuilder imb = new InventoryMenuBuilder(54).withTitle("ZoneUtils - " + ChatColor.stripColor(itemName));

				ArrayList<ItemStack> itemStacks = new ArrayList<>();
				itemStacks.add(new ItemBuilder(Material.SIGN).setTitle(ChatColor.GREEN + tl("GUI_Flags", true)).build());
				itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GREEN + tl("GUI_Members", true)).build());
				itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GREEN + tl("GUI_Owners", true)).build());
				itemStacks.add(new ItemBuilder(Material.BARRIER).setTitle(ChatColor.GREEN + tl("GUI_Delete", true)).build());

				PageLayout pageLayout = new PageLayout("XXXXXXXXX", "XXXXOXXXX", "XXXXXXXXX", "XXOXXXOXX", "XXXXXXXXX", "XXXXOXXXX");
				imb.withItems(pageLayout.generate(itemStacks));

				imb.show(player);
				imb.onInteract(settingsMenuListener, ClickType.LEFT);
			}
		}
	};

	@Override
	public String getPermission() {
		return Variables.PlayerCommands.MAINPERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone";
	}

	@Override
	public String getDescription() {
		return tl("Main_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 0;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (_flagsCache == null || _flagsCache.isEmpty())
			_flagsCache.addAll(Properties.getFlags().stream().filter(flag -> flag instanceof StateFlag || flag instanceof BooleanFlag).collect(Collectors.toList()));

		Player player = (Player) sender;

		HashMap<String, List<String>> zones = myZone.worldGuardHelper.getPlayerRegions(player.getUniqueId());

		List<String> owned  = zones.get("owned");
		List<String> member = zones.get("member");

		int zonesSize   = zones.size();
		int membersSize = member.size();
		int size        = zonesSize + membersSize;

		if (size == 0)
		{
			player.sendMessage(tl("Error") + " " + tl("no_zones"));
			return;
		}

		ArrayList<ItemStack> itemStacks = new ArrayList<>();
		for (String s : owned)
		{
			itemStacks.add(new ItemBuilder(Material.WOOD_DOOR).setTitle(ChatColor.YELLOW + s).build());
		}

		for (String s : member)
		{
			itemStacks.add(new ItemBuilder(Material.ACACIA_DOOR_ITEM).setTitle(ChatColor.YELLOW + s).build());
		}

		if (itemStacks == null)
		{
			player.sendMessage(tl("Error") + " " + "&7Please contact a server administrator.");
			myZone.log("ERROR! Please send this to the plugin's author: ");
			myZone.log("--------- StackTrace ----------");
			myZone.log("Zones Size: " + owned.size());
			myZone.log("Zones: " + owned.toString());
			myZone.log("Members Size: " + member.size());
			myZone.log("Members: " + member.toString());
			myZone.log("Overall Size: " + size);
			myZone.log("--------- End of StackTrace ----------");
		}

		PageInventory pageInventory = new PageInventory("myZone GUI", itemStacks);
		pageInventory.show(player);

		pageInventory.onInteract(mainMenuListener, ClickType.LEFT);
	}
}
