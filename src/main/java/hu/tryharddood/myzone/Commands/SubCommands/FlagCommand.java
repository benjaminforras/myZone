package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class FlagCommand extends Subcommand {
	//private static ArrayList<String> _flags = new ArrayList<>();
	private static String _flags;

	@Override
	public String getPermission() {
		return Variables.PlayerCommands.FLAGPERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone flag <zone> <flag> <value>";
	}

	@Override
	public String getDescription() {
		return tl("FlagZone_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return -1;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (_flags == null || _flags.length() == 0)
		{
			List<Flag<?>> flags = Properties.getFlags();

			StringBuilder stringBuilder = new StringBuilder();
			for (Flag flag : flags)
			{
				stringBuilder.append(flag.getName()).append(", ");
			}
			_flags = stringBuilder.toString();
		}

		if (args.length < 4)
		{
			sender.sendMessage(tl("Wrong") + " " + tl("Command_Usage", getUsage(), getDescription()));
			return;
		}

		String[] strings = Arrays.copyOfRange(args, 2, args.length);
		String   value   = getArgString(strings, 1);

		Player player = (Player) sender;

		String regionID = myZone.zoneManager.getRegionID(args[1]);
		if (regionID == null)
		{
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region  = myZone.worldGuardHelper.getRegion(regionID);
		LocalPlayer     lcOwner = myZone.worldGuardReflection.getWorldGuardPlugin().wrapPlayer(player);

		if (region.getOwners() == null || !region.getOwners().contains(lcOwner) && !player.hasPermission(Variables.PlayerCommands.FLAGOTHERSPERMISSION))
		{
			sender.sendMessage(tl("Error") + " " + tl("FlagZone_Error1"));
			return;
		}

		Flag flag = myZone.worldGuardReflection.fuzzyMatchFlag(args[2]);
		if (flag == null)
		{
			sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error2"));
			sender.sendMessage(tl("FlagZone_AvailableFlags"));
			/*for (String flagName : _flags)
			{
				sender.sendMessage(ChatColor.GOLD + "- " + flagName);
			}*/

			String[] lines = splitSentence(_flags, 5);
			for (String line : lines)
			{
				sender.sendMessage(ChatColor.GOLD + "- " + line);
			}
			return;
		}

		if (flag instanceof StateFlag)
		{
			if (!value.contains("allow") && !value.contains("deny") && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error3"));
				return;
			}
		}
		else if (flag instanceof IntegerFlag)
		{
			if (!isNumeric(value) && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error4"));
				return;
			}
		}
		else if (flag instanceof DoubleFlag)
		{
			if (!isDouble(value) && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error5"));
				return;
			}
		}
		else if (flag instanceof BooleanFlag)
		{
			if (!value.contains("allow") && !value.contains("deny") && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error6"));
				return;
			}
		}
		else if (flag instanceof LocationFlag)
		{
			if (!Pattern.compile("\\[,\\]+").matcher(value).find() && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error9"));
				return;
			}
		}
		else if (flag instanceof SetFlag)
		{
			if (!Pattern.compile("\\[,\\]+").matcher(value).find() && !value.contains("none"))
			{
				sender.sendMessage(tl("Wrong") + " " + tl("FlagZone_Error9"));
				return;
			}
		}

		if (!player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", flag.getName())) && !player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", "*")))
		{
			sender.sendMessage(tl("Error") + " " + tl("FlagZone_Error8"));
			return;
		}

		if (Properties.getEconomyEnabled())
		{
			if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneFlagMoney()))
			{
				sender.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneFlagMoney()));
				return;
			}
			myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneFlagMoney());
		}

		try
		{
			//region.setFlag(flag, flag.parseInput((FlagContext.create().setSender(sender).setInput(value).setObject("region", region).build())));
			region.setFlag(flag, myZone.worldGuardReflection.parseInput(flag, sender, region, value));

		} catch (InvalidFlagFormat invalidFlagFormat)
		{
			sender.sendMessage(tl("Error") + " " + tl("FlagZone_LOL"));
			return;
		}

		sender.sendMessage(tl("Success") + " " + tl("FlagZone_Success", flag.getName(), ((value.contains("allow") || value.contains("true")) ? ChatColor.GREEN : ChatColor.RED) + value));
		myZone.worldGuardHelper.saveAll();
	}

	private static String[] splitSentence(String sentence, int amount) {

		String[] words     = sentence.split(" ");
		int      arraySize = (int) Math.ceil((double) words.length / amount);

		String[] output    = new String[arraySize];
		int      index     = 0;
		int      fullLines = (int) Math.floor((double) words.length / amount);

		for (int i = 0; i < fullLines; i++)
		{
			String appender = "";
			for (int j = 0; j < amount; j++)
			{
				appender += words[index] + " ";
				index++;
			}
			output[i] = appender;
		}

		String appender = "";
		for (int i = index; i < words.length; i++)
		{
			appender += words[index] + " ";
			index++;
		}
		output[fullLines] = appender;
		return output;
	}
}

