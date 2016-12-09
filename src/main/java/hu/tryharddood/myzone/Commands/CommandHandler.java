package hu.tryharddood.myzone.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

import static hu.tryharddood.myzone.Util.I18n.tl;


public class CommandHandler implements CommandExecutor {
	private static HashMap<List<String>, Subcommand> commands = new HashMap<>();

	public static void addComand(List<String> cmds, Subcommand s) {
		commands.put(cmds, s);
	}

	public static HashMap<List<String>, Subcommand> getCommands() {
		return commands;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1)
		{
			boolean match = false;

			for (List<String> s : commands.keySet())
			{
				if (s.contains(args[0]))
				{
					commands.get(s).runCommand(sender, cmd, label, args);
					match = true;
				}
			}

			if (!match)
			{
				sender.sendMessage(tl("Wrong") + " " + tl("Command_NotFound"));
				sender.sendMessage(tl("Command_HelpMessage", "/zone help"));
			}
		}
		else
		{
			commands.keySet().stream().filter(s -> s.contains("gui")).forEach(s -> commands.get(s).runCommand(sender, cmd, label, args));
		}
		return true;
	}
}
