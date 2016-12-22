package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Util.WGWrapper;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;
import static hu.tryharddood.myzone.Variables.PlayerCommands.INFO_PERMISSION;


public class InfoCommand extends Subcommand {

	@Override
	public String getPermission() {
		return INFO_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone info <zone>";
	}

	@Override
	public String getDescription() {
		return tl("Info_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 2;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		String zoneName = args[1];
		String regionID = myZone.getZoneManager().getRegionID(args[1]);
		if (regionID == null)
		{
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region        = WGWrapper.getRegion(regionID);
		RegionManager   regionManager = WGWrapper.getRegionManager(regionID);

		player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.GOLD + zoneName);
		player.sendMessage(ChatColor.GRAY + tl("Text_Owners", true) + ": ");
		if (region.getOwners().getUniqueIds().size() == 0)
		{
			player.sendMessage(ChatColor.GOLD + "- " + "None");
		}
		else
		{
			for (UUID s : region.getOwners().getUniqueIds())
			{
				player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
			}
		}

		player.sendMessage(ChatColor.GRAY + tl("Text_Location", true) + ": " + ChatColor.GOLD + regionManager.getName() + " - " + (region.getMinimumPoint() + " -> " + region.getMaximumPoint()));
		player.sendMessage(ChatColor.GRAY + tl("Text_Members", true) + ": ");
		if (region.getMembers().getUniqueIds().size() == 0)
		{
			player.sendMessage(ChatColor.GOLD + "- " + "None");
		}
		else
		{
			for (UUID s : region.getMembers().getUniqueIds())
			{
				player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
			}
		}

		String temp = "";
		for (Flag flag : region.getFlags().keySet())
		{
			temp += flag.getName() + ", ";
		}
		if (!Objects.equals(temp, ""))
		{
			temp = temp.substring(0, temp.length() - 2);
		}
		player.sendMessage(ChatColor.GRAY + tl("Text_Flags", true) + ": " + ChatColor.GOLD + (temp));
		player.sendMessage(ChatColor.GRAY + tl("Text_Type", true) + ": " + ChatColor.GOLD + (region.getType().getName()));
	}
}
