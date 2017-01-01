package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldedit.Vector;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Util.ZoneUtils;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Selection;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


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
		Player player = (Player) sender;
		Selection selection = Selection.getSelection(player);

		if ((selection.getPos1() == null) || (selection.getPos2() == null))
		{
			sender.sendMessage(tl("Wrong") + " " + tl("SuggestItem", Properties.getCreateTool().toString()));
			return;
		}

		if (selection.getPos1World() != selection.getPos2World())
		{
			sender.sendMessage(tl("Error") + " " + tl("VisualizeZone_Error1"));
			return;
		}

		if (Properties.getDisabledWorlds().contains(selection.getPos1World().getName()))
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error2", selection.getPos1World().getName()));
			return;
		}

		if (Properties.getRegex().matcher(args[1]).find())
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error5"));
			return;
		}
		com.sk89q.worldedit.Vector[] vectors    = {selection.getPos1(), selection.getPos2()};
		String                       permission = ZoneUtils.canBuildZone(player, vectors);

		if (!permission.equals(""))
		{
			if (permission.equals("max"))
			{
				String size = myZone.worldGuardHelper.getPlayerOwnedRegions(player.getUniqueId()).size() + "/" + ZoneUtils.getMaxZones(player);
				sender.sendMessage(tl("Error") + " " + tl("ZoneLimitMax", size));
			}

			if (permission.startsWith("size"))
			{
				Vector maxSize = ZoneUtils.getMaxSize(player);
				sender.sendMessage(tl("Error") + " " + tl("ZoneTooBig", permission.split(":")[1], maxSize.getX() + ", " + maxSize.getY() + ", " + maxSize.getZ()));
			}
			return;
		}

		if (Properties.getEconomyEnabled())
		{
			if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneCreationMoney()))
			{
				sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneCreationMoney()));
				return;
			}
			myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneCreationMoney());
		}


		if (myZone.zoneManager.getZones().containsKey(args[1]))
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error3", args[1]));
			return;
		}

		String regionID = player.getUniqueId().toString() + "_" + String.valueOf(new Random().nextInt(1000));
		myZone.worldGuardHelper.createRegion(args[1], regionID, new Vector[]{vectors[0], vectors[1]}, player, player.getWorld());
	}
}
