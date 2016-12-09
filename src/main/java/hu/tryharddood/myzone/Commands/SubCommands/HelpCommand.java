package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.CommandHandler;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Variables;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static hu.tryharddood.myzone.Util.I18n.tl;


public class HelpCommand extends Subcommand {
	private static List<String> help = new ArrayList<>();

	@Override
	public String getPermission() {
		return Variables.PlayerCommands.MAINPERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone help";
	}

	@Override
	public String getDescription() {
		return tl("Help_Command_Description", true);
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
		if (help.isEmpty())
		{
			help.addAll(CommandHandler.getCommands().entrySet().stream().map(entry -> ChatColor.BLUE + entry.getValue().getUsage() + " - " + ChatColor.GRAY + entry.getValue().getDescription()).collect(Collectors.toList()));
		}

		help.forEach(sender::sendMessage);
	}
}
