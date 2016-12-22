package hu.tryharddood.myzone.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public abstract class Subcommand {
	public abstract String getPermission();

	public abstract String getUsage();

	public abstract String getDescription();

	public abstract int getArgs();

	public abstract boolean playerOnly();

	public abstract void onCommand(CommandSender sender, Command cmd, String label, String[] args);

	public boolean runCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(getPermission()))
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_NoPermission"));
		}
		else if (getArgs() != -1 && getArgs() != args.length)
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
		}
		else
		{
			if (playerOnly())
			{
				if (sender instanceof Player)
				{
					onCommand(sender, cmd, label, args);
				}
				else
				{
					sender.sendMessage(tl("Error") + " " + tl("Command_PlayerOnly"));
				}
			}
			else
			{
				onCommand(sender, cmd, label, args);
			}
		}

		return true;
	}

	protected boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	protected boolean isDouble(String s) {
		try
		{
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}

	protected String getArgString(String[] args, int start) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < args.length; i++)
		{
			sb.append(args[i]).append(" ");
		}

		return sb.toString().trim();
	}
}
