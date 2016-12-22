package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Util.ParticleEffects.ParticleEffect;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Settings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;

/*****************************************************
 *              Created by TryHardDood on 2016. 07. 23..
 ****************************************************/
public class VisualizeCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.PlayerCommands.VISUALIZE_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone visualize";
	}

	@Override
	public String getDescription() {
		return tl("Command_Visualize_Description", true);
	}

	@Override
	public int getArgs() {
		return 1;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player     player    = (Player) sender;
		Settings   sett      = Settings.getSett(player);
		Location[] locations = {sett.getBorder1(), sett.getBorder2()};

		if ((locations[0] == null) || (locations[1] == null))
		{
			sender.sendMessage(tl("Wrong") + " " + tl("SuggestItem", Properties.getCreateTool().toString()));
			return;
		}

		if (locations[0].getWorld() != locations[1].getWorld())
		{
			sender.sendMessage(tl("Error") + " " + tl("VisualizeZone_Error1"));
			return;
		}

		if (Properties.getDisabledWorlds().contains(locations[0].getWorld().getName()))
		{
			sender.sendMessage(tl("Error") + " " + tl("VisualizeZone_Error2", locations[0].getWorld().getName()));
			return;
		}

		int minx = Math.min(locations[0].getBlockX(), locations[1].getBlockX());
		int maxx = Math.max(locations[0].getBlockX(), locations[1].getBlockX());
		int miny = Math.min(locations[0].getBlockY(), locations[1].getBlockY());
		int maxy = Math.max(locations[0].getBlockY(), locations[1].getBlockY());
		int minz = Math.min(locations[0].getBlockZ(), locations[1].getBlockZ());
		int maxz = Math.max(locations[0].getBlockZ(), locations[1].getBlockZ());

		World world = locations[0].getWorld();

		Location loc = player.getLocation();
		loc.add((double) Properties.getViewDistance(), (double) Properties.getViewDistance(), (double) Properties.getViewDistance());

		Block b;
		for (int x = minx; x <= maxx; x++)
		{
			for (int y = miny; y <= maxy; y++)
			{
				for (int z = minz; z <= maxz; z++)
				{
					if (loc.getX() < x || loc.getY() < y || loc.getZ() < z)
					{
						break;
					}

					b = world.getBlockAt(x, y, z);
					if (isOutline(b.getLocation(), minx, maxx, miny, maxy, minz, maxz))
					{
						ParticleEffect.FIREWORKS_SPARK.display(0, 0, 0, 0, 1, b.getLocation(), player);
					}
				}
			}
		}
	}

	private boolean isOutline(Location l, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		return (x == xmin || x == xmax || y == ymin || y == ymax || z == zmin || z == zmax);
	}
}
