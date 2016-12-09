package hu.tryharddood.myzone.Commands.SubCommands;

import hu.tryharddood.myzone.Commands.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/*****************************************************
 * Created by TryHardDood on 2016. 07. 08..
 ****************************************************/
public class VersionCommand extends Subcommand {

	private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(VersionCommand.class);

	@Override
	public String getPermission() {
		return "";
	}

	@Override
	public String getUsage() {
		return "/zone version";
	}

	@Override
	public String getDescription() {
		return "Displays every information about this plugin";
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
		sender.sendMessage(ChatColor.BLUE + "Author:" + ChatColor.GRAY + plugin.getDescription().getAuthors());
		sender.sendMessage(ChatColor.BLUE + "Version:" + ChatColor.GRAY + plugin.getDescription().getVersion());
		sender.sendMessage(ChatColor.BLUE + "Description:" + ChatColor.GRAY + plugin.getDescription().getDescription());
		sender.sendMessage(ChatColor.BLUE + "Website:" + ChatColor.GRAY + plugin.getDescription().getWebsite());
	}
}
