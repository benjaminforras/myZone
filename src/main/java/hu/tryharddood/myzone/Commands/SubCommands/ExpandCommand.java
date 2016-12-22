package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Util.WGWrapper;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;
import static hu.tryharddood.myzone.Variables.PlayerCommands.EXPAND_PERMISSION;


public class ExpandCommand extends Subcommand {
	@Override
	public String getPermission() {
		return EXPAND_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone expand <zone> <size> <up|down|north|east|south|west>";
	}

	@Override
	public String getDescription() {
		return tl("ExpandZone_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 4;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player   = (Player) sender;
		String regionID = myZone.getZoneManager().getRegionID(args[1]);

		if (regionID == null)
		{
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region  = WGWrapper.getRegion(regionID);
		LocalPlayer     lcOwner = myZone.getWgPlugin().wrapPlayer(player);
		if (region.getOwners() == null || !region.getOwners().contains(lcOwner) && !player.hasPermission(Variables.PlayerCommands.EXPAND_OTHERS_PERMISSION))
		{
			sender.sendMessage(tl("Error") + " " + tl("ExpandZone_Error1"));
			return;
		}

		if (Properties.getEconomyEnabled())
		{
			if (!myZone.getEconomy().has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneExpandMoney()))
			{
				sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneExpandMoney()));
				return;
			}
			myZone.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneExpandMoney());
		}

		Integer expanse = -1;

		try
		{
			expanse = Integer.parseInt(args[2]);
		} catch (NumberFormatException e)
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
			return;
		}

		String    direction = args[3];
		BlockFace blockFace;

		if (direction.equalsIgnoreCase("up"))
		{
			blockFace = BlockFace.UP;
		}
		else if (direction.equalsIgnoreCase("down"))
		{
			blockFace = BlockFace.DOWN;
		}
		else if (direction.equalsIgnoreCase("north"))
		{
			blockFace = BlockFace.NORTH;
		}
		else if (direction.equalsIgnoreCase("east"))
		{
			blockFace = BlockFace.EAST;
		}
		else if (direction.equalsIgnoreCase("south"))
		{
			blockFace = BlockFace.SOUTH;
		}
		else if (direction.equalsIgnoreCase("west"))
		{
			blockFace = BlockFace.WEST;
		}
		else
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
			return;
		}

		int data = WGWrapper.expandRegion(player, region, blockFace, expanse);
		if (data == -2)
		{
			sender.sendMessage(tl("Error") + " " + tl("ExpandZone_Error3", args[1], expanse, blockFace.toString()));

		}
		else if (data == -3)
		{
			sender.sendMessage(tl("Error") + " " + tl("ExpandZone_Error2", args[1], expanse, blockFace.toString()));
		}
		else if (data == -1)
		{
			sender.sendMessage(tl("Error") + " " + tl("ExpandZone_Error2", args[1], expanse, blockFace.toString()));
		}
		else
		{
			sender.sendMessage(tl("Success") + " " + tl("ExpandZone_Success", args[1], expanse, blockFace.toString()));
		}
	}
}