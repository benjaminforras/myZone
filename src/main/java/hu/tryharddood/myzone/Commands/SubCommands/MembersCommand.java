package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class MembersCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.PlayerCommands.MEMBERS_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone members <zone> <add|remove> <player>";
	}

	@Override
	public String getDescription() {
		return tl("MembersZone_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 4;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String action     = args[2];
		String targetName = args[3];

		Player player   = (Player) sender;
		String regionID = myZone.zoneManager.getRegionID(args[1]);
		if (regionID == null)
		{
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region = myZone.worldGuardHelper.getRegion(regionID);

		if (action.equalsIgnoreCase("add"))
		{
			Player player1 = (Player) sender;
			if (!player1.hasPermission(Variables.PlayerCommands.MEMBERS_ADD_PERMISSION))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("Command_NoPermission"));
				return;
			}

			LocalPlayer lcOwner = myZone.worldGuardReflection.getWorldGuardPlugin().wrapPlayer(player1);
			if (region.getOwners() == null || !region.getOwners().contains(lcOwner) && !player1.hasPermission(Variables.PlayerCommands.MEMBERS_ADD_OTHERS_PERMISSION))
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Add_Error1"));
				return;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
			if (target == null)
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Add_Error2", targetName));
				return;
			}

			LocalPlayer lcPlayer = myZone.worldGuardReflection.getWorldGuardPlugin().wrapOfflinePlayer(target);

			if (region.getMembers().contains(lcPlayer))
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Add_Error3", targetName));
				return;
			}

			if (myZone.config.economy.enabled)
			{
				if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.member.memberAdd))
				{
					sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", myZone.config.economy.member.memberAdd));
					return;
				}
				myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.member.memberAdd);
			}

			region.getMembers().addPlayer(lcPlayer);
			sender.sendMessage(tl("Success") + " " + tl("MembersZone_Add_Success", lcPlayer.getName()));
		}
		else if (action.equalsIgnoreCase("remove"))
		{
			Player player1 = (Player) sender;
			if (!player1.hasPermission(Variables.PlayerCommands.MEMBERS_REMOVE_PERMISSION))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("Command_NoPermission"));
				return;
			}

			if (!region.getOwners().contains(player1.getName()) && !region.getOwners().contains(player1.getUniqueId()) && !player1.hasPermission(Variables.PlayerCommands.MEMBERS_REMOVE_OTHERS_PERMISSION))
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Remove_Error1"));
				return;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
			if (target == null)
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Remove_Error2", targetName));
				return;
			}

			LocalPlayer lcPlayer = myZone.worldGuardReflection.getWorldGuardPlugin().wrapOfflinePlayer(target);

			if (!region.getMembers().contains(lcPlayer))
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Remove_Error3", lcPlayer.getName()));
				return;
			}

			if (myZone.config.economy.enabled)
			{
				if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.member.memberRemove))
				{
					sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", myZone.config.economy.member.memberRemove));
					return;
				}
				myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), myZone.config.economy.member.memberRemove);
			}

			region.getMembers().removePlayer(lcPlayer);

			sender.sendMessage(tl("Success") + " " + tl("MembersZone_Remove_Success", lcPlayer.getName()));
		}
		myZone.worldGuardHelper.saveAll();
	}
}
