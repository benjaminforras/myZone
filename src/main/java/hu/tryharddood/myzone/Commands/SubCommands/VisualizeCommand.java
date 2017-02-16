package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldedit.BlockVector;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Selection;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.inventivetalent.particle.ParticleEffect;

import java.util.Collections;

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
		Player                       player    = (Player) sender;
		Selection                    selection = Selection.getSelection(player);
		com.sk89q.worldedit.Vector[] vectors   = new com.sk89q.worldedit.Vector[]{selection.getPos1(), selection.getPos2()};

		if ((vectors[0] == null) || (vectors[1] == null))
		{
			sender.sendMessage(tl("Wrong") + " " + tl("SuggestItem", myZone.config.createTool));
			return;
		}

		if (selection.getPos1World() != selection.getPos2World())
		{
			sender.sendMessage(tl("Error") + " " + tl("VisualizeZone_Error1"));
			return;
		}

		if (myZone.config.disabledWorlds.contains(selection.getPos1World().getName()))
		{
			sender.sendMessage(tl("Error") + " " + tl("CreateZone_Error2", selection.getPos1World().getName()));
			return;
		}

		BukkitTask iTaskID = new BukkitRunnable() {
			@Override
			public void run() {
				for (BlockVector lo : selection.getCuboidRegion().getFaces())
				{
					Location l = new Location(player.getWorld(), lo.getX() + 0.5, lo.getY() + 0.5, lo.getZ() + 0.5);

					try
					{
						ParticleEffect.VILLAGER_HAPPY.send(Collections.singletonList(player), l.clone(), 0, 0, 0, 0, 1);

					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}.runTaskTimerAsynchronously(myZone.myZonePlugin, 5L, 20L);

		new BukkitRunnable() {
			@Override
			public void run() {
				iTaskID.cancel();
			}
		}.runTaskLater(myZone.myZonePlugin, 90L);
	}

}
