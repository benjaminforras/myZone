package hu.tryharddood.myzone;

import com.sk89q.worldguard.protection.flags.Flag;
import hu.tryharddood.myzone.Listeners.rListener;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;


public class Properties {
	private static final myZone _instance = myZone.myZonePlugin;

	private static String _locale;

	private static Material _createTool;
	private static Material _checkTool;

	private static Integer _checkToolDelay;

	private static Boolean _economyEnabled;
	private static Boolean _titlesEnabled;
	private static Boolean _actionbarEnabled;
	private static Boolean _bossbarEnabled;

	private static Pattern _regex;

	private static Integer _zoneCreationMoney;
	private static Integer _zoneDeleteMoney;
	private static Integer _zoneFlagMoney;
	private static Integer _zoneExpandMoney;
	private static Integer _zoneMemberAddMoney;
	private static Integer _zoneMemberRemoveMoney;
	private static Integer _zoneOwnerAddMoney;
	private static Integer _zoneOwnerRemoveMoney;

	private static Integer _viewdistance;

	private static List<String> _disabledworlds = new ArrayList<>();

	private static List<Flag<?>> _flags        = new ArrayList<>();
	private static List<Flag<?>> _blockedFlags = new ArrayList<>();

	private static HashMap<String, Integer>                    _maxZonePermission     = new HashMap<>();
	private static HashMap<String, com.sk89q.worldedit.Vector> _maxZoneSizePermission = new HashMap<>();

	public static void loadConfiguration() {
		_instance.saveDefaultConfig();
		_instance.reloadConfig();
		FileConfiguration config = _instance.getConfig();

		_locale = config.getString("Locale", "en");

		_createTool = Material.matchMaterial(config.getString("createTool", Material.WOOD_SPADE.toString()));
		_checkTool = Material.matchMaterial(config.getString("checkTool", Material.WOOD_SWORD.toString()));

		_checkToolDelay = config.getInt("Delay.checkTool", 5);


		String regex = config.getString("Zone.Regex", "[^a-zA-Z0-9_\\p{L}+]");

		try
		{
			_regex = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
		} catch (PatternSyntaxException exception)
		{
			myZone.log("Error loading your specified regex: " + regex);
			myZone.log("Setting default: [^a-zA-Z0-9_\\p{L}+]");
			_regex = Pattern.compile("[^a-zA-Z0-9_\\p{L}+]", Pattern.UNICODE_CHARACTER_CLASS);
		}

		_economyEnabled = config.getBoolean("Economy.Enabled", false);
		if (getEconomyEnabled())
		{
			_zoneCreationMoney = config.getInt("Economy.Create", 50);
			_zoneDeleteMoney = config.getInt("Economy.Delete", 50);
			_zoneFlagMoney = config.getInt("Economy.Flag", 50);
			_zoneExpandMoney = config.getInt("Economy.Expand", 50);
			_zoneMemberAddMoney = config.getInt("Economy.Member.Add", 50);
			_zoneMemberRemoveMoney = config.getInt("Economy.Member.Remove", 50);
			_zoneOwnerAddMoney = config.getInt("Economy.Owner.Add", 50);
			_zoneOwnerRemoveMoney = config.getInt("Economy.Owner.Remove", 50);

			myZone.log("Trying to hook Vault...");
			setupVault();
		}

		_viewdistance = Bukkit.getViewDistance();

		_titlesEnabled = config.getBoolean("Messages.Title.Enabled", true);
		_actionbarEnabled = config.getBoolean("Messages.ActionBar.Enabled", true);
		_bossbarEnabled = config.getBoolean("Messages.BossBarAPI.Enabled", true);

		if (_titlesEnabled || _actionbarEnabled || _bossbarEnabled)
		{
			rListener rListener = new rListener(_instance, myZone.worldGuardReflection.getWorldGuardPlugin());
			_instance.getServer().getPluginManager().registerEvents(rListener, myZone.worldGuardReflection.getWorldGuardPlugin());
		}

		for (String key : config.getConfigurationSection("Restriction.Size").getKeys(true))
		{
			com.sk89q.worldedit.Vector size = toVector(getVector(key));
			if (size == null)
			{
				continue;
			}

			String permission = config.getString("Restriction.Size." + key, "none");
			if (permission.equals("none"))
			{
				continue;
			}
			_maxZoneSizePermission.put(permission, size);
		}

		for (String key : config.getConfigurationSection("Restriction.Zone").getKeys(true))
		{
			int size = 0;
			try
			{
				size = Integer.parseInt(key);
			} catch (Exception e)
			{
				continue;
			}
			String permission = config.getString("Restriction.Zone." + key, "none");
			if (permission.equals("none"))
			{
				continue;
			}
			_maxZonePermission.put(permission, size);
		}

		Flag<?> tempFlag;
		for (String flagName : config.getStringList("Restriction.BlockedFlags"))
		{
			tempFlag = myZone.worldGuardReflection.fuzzyMatchFlag(flagName);
			_blockedFlags.add(tempFlag);
		}

		Flag<?>[] flags = myZone.worldGuardReflection.getFlags();
		for (Flag flag : flags)
		{
			if (!_blockedFlags.contains(flag))
			{
				_flags.add(flag);
			}
		}

		_disabledworlds.addAll(config.getStringList("DisabledWorlds"));

		copyLangConfig();
		System.out.println("[" + _instance.getDescription().getName() + " v" + _instance.getDescription().getVersion() + "] " + "Configuration successfully loaded.");
	}

	public static HashMap<String, Integer> getMaxZonePermissions() {
		return _maxZonePermission;
	}

	public static HashMap<String, com.sk89q.worldedit.Vector> getMaxZoneSizePermissions() {
		return _maxZoneSizePermission;
	}

	public static Material getCreateTool() {
		if (_createTool == null)
		{
			_createTool = Material.WOOD_SPADE;
		}
		return _createTool;
	}

	public static Material getCheckTool() {
		if (_checkTool == null)
		{
			_checkTool = Material.WOOD_SWORD;
		}
		return _checkTool;
	}

	public static Integer getZoneCreationMoney() {
		return _zoneCreationMoney;
	}

	public static Integer getZoneDeleteMoney() {
		return _zoneDeleteMoney;
	}

	public static Integer getZoneFlagMoney() {
		return _zoneFlagMoney;
	}

	public static Integer getZoneMemberAddMoney() {
		return _zoneMemberAddMoney;
	}

	public static Integer getZoneMemberRemoveMoney() {
		return _zoneMemberRemoveMoney;
	}

	public static Boolean getEconomyEnabled() {
		return _economyEnabled;
	}

	public static void setEconomyEnabled(Boolean value) {
		_economyEnabled = value;
	}

	public static List<String> getDisabledWorlds() {
		return _disabledworlds;
	}

	public static boolean setupEconomy() {
		try
		{
			RegisteredServiceProvider<Economy> economyProvider = _instance.getServer().getServicesManager().getRegistration(Economy.class);

			if (economyProvider != null)
			{
				myZone.vaultEcon = economyProvider.getProvider();
			}
			else
			{
				myZone.log("- No economy plugin found! This plugin may not work properly.");
				Properties.setEconomyEnabled(false);
				return false;
			}
		} catch (NoClassDefFoundError ex)
		{
			myZone.log("- No economy plugin found! This plugin may not work properly.");
			Properties.setEconomyEnabled(false);
			return false;
		}
		return (myZone.vaultEcon != null);
	}

	private static void setupVault() {
		Plugin vault = _instance.getServer().getPluginManager().getPlugin("Vault");

		if ((vault != null) && (vault instanceof Vault))
		{
			myZone.log("Vault (v" + vault.getDescription().getVersion() + ") successfully hooked.");

			if (!setupEconomy())
			{
				myZone.log("- No economy plugin found!");
				Properties.setEconomyEnabled(false);
			}
			else
			{
				myZone.log("Found an economy plugin. Using it.");
			}
		}
		else
		{
			myZone.log("Can't find Vault. Disabling economy support");
			Properties.setEconomyEnabled(false);
		}
	}

	private static Vector getVector(String value) {
		Vector   vector = new Vector(0, 0, 0);
		String[] split  = value.split(", ");
		try
		{
			vector.setX(Double.parseDouble(split[0].substring(1)));
			vector.setY(Double.parseDouble(split[1]));
			vector.setZ(Double.parseDouble(split[2].substring(0, split[2].length() - 1)));
		} catch (Exception e)
		{
			return null;
		}
		return vector;
	}

	private static void copyLangConfig() {
		String localeFileName = "messages_" + _locale + ".properties";
		if (Objects.equals(_locale, "en"))
		{
			localeFileName = "messages.properties";
		}

		File _langFile;
		try
		{
			_langFile = new File(_instance.getDataFolder(), localeFileName);
			if (!_langFile.exists())
			{
				_instance.saveResource(localeFileName, false);
			}
		} catch (IllegalArgumentException iaex)
		{
			_langFile = new File(_instance.getDataFolder(), "messages" + ".properties");
			if (!_langFile.exists())
			{
				_instance.saveResource("messages" + ".properties", false);
			}
			_locale = "en";
			_instance.getLogger().log(Level.WARNING, "Wrong translation file setup, using \"en\"");
		}
		_instance.getI18n().updateLocale(_locale);
	}

	public static List<Flag<?>> getFlags() {
		return _flags;
	}

	public static String getLocale() {
		return _locale;
	}

	public static Integer getZoneExpandMoney() {
		return _zoneExpandMoney;
	}

	public static Integer getCheckToolDelay() {
		return _checkToolDelay;
	}

	public static Boolean getTitlesEnabled() {
		return _titlesEnabled;
	}

	public static Boolean getActionBarEnabled() {
		return _actionbarEnabled;
	}

	public static Boolean getBossBarEnabled() {
		return _bossbarEnabled;
	}

	public static Pattern getRegex() {
		return _regex;
	}

	public static Integer getZoneOwnerAddMoney() {
		return _zoneOwnerAddMoney;
	}

	public static Integer getZoneOwnerRemoveMoney() {
		return _zoneOwnerRemoveMoney;
	}

	/*public static Integer getViewDistance() {
		return _viewdistance;
	}*/
}
