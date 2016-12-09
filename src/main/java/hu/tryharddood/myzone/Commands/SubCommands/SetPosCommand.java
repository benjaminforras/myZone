package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Zones.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.myzone.Util.I18n.tl;
import static hu.tryharddood.myzone.Variables.PlayerCommands.SETPOS_PERMISSION;

/*****************************************************
 * Created by TryHardDood on 2016. 07. 09..
 ****************************************************/
public class SetPosCommand extends Subcommand {
	@Override
	public String getPermission() {
		return SETPOS_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone setpos <1|2>";
	}

	@Override
	public String getDescription() {
		return tl("SetPos_Command_Description", true);
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

		String   data     = args[1];
		Player   player   = (Player) sender;
		Settings settings = Settings.getSett(player);

		if (data.equalsIgnoreCase("1"))
		{
			settings.setBorder(1, player.getLocation());
			player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "1."));
		}
		else if (data.equalsIgnoreCase("2"))
		{
			settings.setBorder(2, player.getLocation());
			player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "2."));
		}
		else
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
		}
	}
}
