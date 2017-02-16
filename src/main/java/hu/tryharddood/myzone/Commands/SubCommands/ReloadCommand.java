package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class ReloadCommand extends Subcommand {
	@Override
	public String getPermission() {
		return Variables.PlayerCommands.RELOAD_PERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone reload";
	}

	@Override
	public String getDescription() {
		return tl("Reload_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 1;
	}

	@Override
	public boolean playerOnly() {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		myZone.zoneManager.loadZones();
		myZone.myZonePlugin.loadConfiguration();

		sender.sendMessage(tl("Success") + " " + tl("Reload_Success"));
	}
}
