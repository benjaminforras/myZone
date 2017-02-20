package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;

/*****************************************************
 *              Created by TryHardDood on 2016. 07. 22..
 ****************************************************/
public class OwnersCommand extends Subcommand
{
	@Override
	public String getPermission()
	{
		return Variables.PlayerCommands.OWNERS_PERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/zone owners <zone> <add|remove> <player>";
	}

	@Override
	public String getDescription()
	{
		return tl("OwnersZone_Command_Description");
	}

	@Override
	public int getArgs()
	{
		return 4;
	}

	@Override
	public boolean playerOnly()
	{
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		String action     = args[2];
		String targetName = args[3];

		Player player   = (Player) sender;
		String regionID = myZone.zoneManager.getRegionID(args[1]);
		if (regionID == null) {
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region = myZone.worldGuardHelper.getRegion(regionID);

		if (action.equalsIgnoreCase("add")) {
			Player player1 = (Player) sender;
			if (!player1.hasPermission(Variables.PlayerCommands.OWNERS_ADD_PERMISSION)) {
				sender.sendMessage(tl("Wrong") + " " + tl("Command_NoPermission"));
				return;
			}

			LocalPlayer lcOwner = myZone.worldGuardReflection.getWorldGuardPlugin().wrapPlayer(player1);
			if (region.getOwners() == null || !region.getOwners().contains(lcOwner) && !player1.hasPermission(Variables.PlayerCommands.OWNERS_ADD_OTHERS_PERMISSION)) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Add_Error1"));
				return;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
			if (target == null) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Add_Error2", targetName));
				return;
			}

			LocalPlayer lcPlayer = myZone.worldGuardReflection.getWorldGuardPlugin().wrapOfflinePlayer(target);

			if (region.getOwners().contains(lcPlayer)) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Add_Error3", targetName));
				return;
			}

			if (myZone.config.economy.enabled) {
				if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.owner.ownerAdd)) {
					sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", myZone.config.economy.owner.ownerAdd));
					return;
				}
				myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.owner.ownerAdd);
			}

			region.getOwners().addPlayer(lcPlayer);
			sender.sendMessage(tl("Success") + " " + tl("OwnersZone_Add_Success", lcPlayer.getName()));
			myZone.zoneManager.updateCacheForPlayer(lcPlayer.getUniqueId());
		}
		else if (action.equalsIgnoreCase("remove")) {
			Player player1 = (Player) sender;
			if (!player1.hasPermission(Variables.PlayerCommands.OWNERS_REMOVE_PERMISSION)) {
				sender.sendMessage(tl("Wrong") + " " + tl("Command_NoPermission"));
				return;
			}

			if (!region.getOwners().contains(player1.getName()) && !region.getOwners().contains(player1.getUniqueId()) && !player1.hasPermission(Variables.PlayerCommands.OWNERS_REMOVE_OTHERS_PERMISSION)) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Remove_Error1"));
				return;
			}


			OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
			if (target == null) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Remove_Error2", targetName));
				return;
			}

			if (myZone.zoneManager.getZoneObject(args[1]).getOwnerID().equals(target.getUniqueId()) && !player.hasPermission(Variables.PlayerCommands.OWNERS_REMOVE_OG_PERMISSION)) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Remove_Error4"));
				return;
			}

			LocalPlayer lcPlayer = myZone.worldGuardReflection.getWorldGuardPlugin().wrapOfflinePlayer(target);

			if (!region.getOwners().contains(lcPlayer)) {
				sender.sendMessage(tl("Error") + " " + tl("OwnersZone_Remove_Error3", lcPlayer.getName()));
				return;
			}

			if (region.getOwners().size() - 1 == 0) {
				InventoryMenuBuilder imb = new InventoryMenuBuilder(27).withTitle(tl("GUI_AreYouSure"));
				imb.withItem(11, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + tl("GUI_Confirm")).build());
				imb.withItem(13, new ItemBuilder(Material.PAPER).setTitle(tl("GUI_Description")).addLore(tl("GUI_DeleteLastOwner")).build());
				imb.withItem(15, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + tl("GUI_Cancel")).build());
				imb.show(player);

				imb.onInteract((player_, action_, event_) -> {
					if (event_.getCurrentItem() == null) return;

					if (event_.getCurrentItem().getDurability() == (short) 13) {
						List<UUID> players = new ArrayList<>();
						players.addAll(region.getMembers().getUniqueIds());
						players.addAll(region.getOwners().getUniqueIds());
						players.add(player_.getUniqueId());

						try {
							myZone.worldGuardHelper.deleteRegion(region);
							myZone.worldGuardHelper.saveAll();
						}
						finally {
							myZone.zoneManager.updateCacheForPlayers(players.toArray(new UUID[players.size()]));
						}
						sender.sendMessage(tl("Success") + " " + tl("OwnersZone_Remove_Success", lcPlayer.getName()));
						sender.sendMessage(tl("Success") + " " + tl("DeleteZone_Success", args[1]));
					}
					player_.closeInventory();
				}, ClickType.LEFT);
				return;
			}

			if (myZone.config.economy.enabled) {
				if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.owner.ownerRemove)) {
					sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", myZone.config.economy.owner.ownerRemove));
					return;
				}
				myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.owner.ownerRemove);
			}

			region.getOwners().removePlayer(lcPlayer);

			sender.sendMessage(tl("Success") + " " + tl("OwnersZone_Remove_Success", lcPlayer.getName()));
			myZone.zoneManager.updateCacheForPlayer(lcPlayer.getUniqueId());
		}
		myZone.worldGuardHelper.saveAll();
	}
}
