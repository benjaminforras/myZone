package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;

/*****************************************************
 *              Created by TryHardDood on 2016. 07. 17..
 ****************************************************/
public class ListCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.PlayerCommands.LIST_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone list [owner]";
	}

	@Override
	public String getDescription() {
		return tl("ListZone_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return -1;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int display = 0;
		if (args.length == 1)
		{
			display = 1;
		}
		else if (args.length == 2)
		{
			display = 2;
		}
		else
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
			return;
		}


		if (display == 1)
		{
			if (!(sender instanceof Player))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("ListZone_Error1"));
				return;
			}
			Player            player   = (Player) sender;
			ArrayList<String> zoneList = myZone.worldGuardHelper.getPlayerOwnedRegions(player.getUniqueId());

			sender.sendMessage(tl("Success") + " " + tl("ListZone_Success", player.getName()));

			StringBuilder sBuilder = new StringBuilder();
			for (String zoneName : zoneList)
			{
				sBuilder.append(ChatColor.WHITE).append(zoneName).append(ChatColor.GRAY).append(", ");
			}
			String message = sBuilder.toString();
			if (message.endsWith(", "))
				message = message.substring(0, message.length() - 2);
			sender.sendMessage(message);
		}
		else
		{
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
			if (offlinePlayer == null)
			{
				sender.sendMessage(tl("Error") + " " + tl("MembersZone_Add_Error2", args[1]));
				return;
			}

			ArrayList<String> zoneList = myZone.worldGuardHelper.getPlayerOwnedRegions(offlinePlayer.getUniqueId());

			sender.sendMessage(tl("Success") + " " + tl("ListZone_Success", offlinePlayer.getName()));

			StringBuilder sBuilder = new StringBuilder();
			for (String zoneName : zoneList)
			{
				sBuilder.append(ChatColor.WHITE).append(zoneName).append(ChatColor.GRAY).append(", ");
			}
			String message = sBuilder.toString();
			if (message.endsWith(", "))
				message = message.substring(0, message.length() - 2);

			sender.sendMessage(message);
		}
	}
}
