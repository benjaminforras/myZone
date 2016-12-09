package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldedit.Vector;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Util.WGWrapper;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Settings;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import static hu.tryharddood.myzone.Util.I18n.tl;


public class CreateCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.PlayerCommands.CREATEPERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone create <name>";
	}

	@Override
	public String getDescription() {
		return tl("CreateZone_Command_Description", true);
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
		Player     player   = (Player) sender;
		Settings   settings = Settings.getSett(player);
		Location[] parts    = {settings.getBorder1(), settings.getBorder2()};

		if ((parts[0] == null) || (parts[1] == null))
		{
			sender.sendMessage(tl("Wrong") + " " + tl("SuggestItem", Properties.getCreateTool().toString()));
			return;
		}

		if (parts[0].getWorld() != parts[1].getWorld())
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error1"));
			return;
		}

		if (Properties.getDisabledWorlds().contains(parts[0].getWorld().getName()))
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error2", parts[0].getWorld().getName()));
			return;
		}


		if (Properties.getRegex().matcher(args[1]).find())
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error5"));
			return;
		}

		String permission = Settings.canBuildZone(player, parts);

		if (!permission.equals(""))
		{
			if (permission.equals("max"))
			{
				String size = settings.getPlayerZones() + "/" + settings.getMaxZones();
				sender.sendMessage(tl("Error") + " " + tl("ZoneLimitMax", size));
			}

			if (permission.startsWith("size"))
			{
				Vector maxSize = settings.getMaxSize();
				sender.sendMessage(tl("Error") + " " + tl("ZoneTooBig", permission.split(":")[1], maxSize.getX() + ", " + maxSize.getY() + ", " + maxSize.getZ()));
			}
			return;
		}

		if (Properties.getEconomyEnabled())
		{
			if (!myZone.getEconomy().has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneCreationMoney()))
			{
				sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneCreationMoney()));
				return;
			}
			myZone.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneCreationMoney());
		}


		if (myZone.getZoneManager().getZones().containsKey(args[1]))
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error3", args[1]));
			return;
		}

		String regionID = player.getUniqueId().toString() + "_" + String.valueOf(new Random().nextInt(1000));
		WGWrapper.createRegion(args[1], regionID, new Vector[]{toVector(parts[0]), toVector(parts[1])}, player, player.getWorld());
	}
}
