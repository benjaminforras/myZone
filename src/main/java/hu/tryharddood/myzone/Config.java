package hu.tryharddood.myzone;

import com.sk89q.worldguard.protection.flags.Flag;
import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.Section;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;
import hu.tryharddood.myzone.Listeners.rListener;
import net.milkbowl.vault.Vault;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Config extends ReflectedYaml
{
	public transient final myZone _instance = myZone.myZonePlugin;

	public transient List<Flag<?>> flags        = new ArrayList<>();
	public transient List<Flag<?>> blockedFlags = new ArrayList<>();

	public transient Material createToolMaterial;
	public transient Material checkToolMaterial;

	void init()
	{
		try {
			Pattern.compile(zone.regex, Pattern.UNICODE_CHARACTER_CLASS);
		}
		catch (PatternSyntaxException exception) {
			myZone.log("Error loading your specified regex: " + zone.regex);
			myZone.log("Setting default: [^a-zA-Z0-9_\\p{L}+]");
		}

		if(messages.title.enabled || messages.actionBar.enabled || messages.bossBar.enabled)
		{
			rListener rListener = new rListener(_instance, myZone.worldGuardReflection.getWorldGuardPlugin());
			_instance.getServer().getPluginManager().registerEvents(rListener, myZone.worldGuardReflection.getWorldGuardPlugin());
		}

		if (economy.enabled) {
			myZone.log("Trying to hook Vault...");
			setupVault();
		}

		Flag<?> tempFlag;
		for (String flagName : restriction.blockedFlags)
		{
			tempFlag = myZone.worldGuardReflection.fuzzyMatchFlag(flagName);
			blockedFlags.add(tempFlag);
		}

		createToolMaterial = Material.matchMaterial(createTool);
		checkToolMaterial = Material.matchMaterial(checkTool);

		Flag<?>[] flags = myZone.worldGuardReflection.getFlags();
		for (Flag flag : flags)
		{
			if (!blockedFlags.contains(flag))
			{
				this.flags.add(flag);
			}
		}
		copyLangConfig();
		System.out.println("[" + _instance.getDescription().getName() + " v" + _instance.getDescription().getVersion() + "] " + "Configuration successfully loaded.");
	}

	private boolean setupEconomy()
	{
		try {
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = _instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

			if (economyProvider != null) {
				myZone.vaultEcon = economyProvider.getProvider();
			}
			else {
				myZone.log("- No economy plugin found! This plugin may not work properly.");
				economy.enabled = false;
				return false;
			}
		}
		catch (NoClassDefFoundError ex) {
			myZone.log("- No economy plugin found! This plugin may not work properly.");
			economy.enabled = false;
			return false;
		}
		return (myZone.vaultEcon != null);
	}

	private void setupVault()
	{
		Plugin vault = _instance.getServer().getPluginManager().getPlugin("Vault");

		if ((vault != null) && (vault instanceof Vault)) {
			myZone.log("Vault (v" + vault.getDescription().getVersion() + ") successfully hooked.");

			if (!setupEconomy()) {
				myZone.log("- No economy plugin found!");
				economy.enabled = false;
			}
			else {
				myZone.log("Found an economy plugin. Using it.");
			}
		}
		else {
			myZone.log("Can't find Vault. Disabling economy support");
			economy.enabled = false;
		}
	}

	private void copyLangConfig() {
		String localeFileName = "messages_" + locale + ".properties";
		if (Objects.equals(locale, "en"))
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
			locale = "en";
			_instance.getLogger().log(Level.WARNING, "Wrong translation file setup, using \"en\"");
		}
		_instance.getI18n().updateLocale(locale);
	}

	@Name("Locale")
	@Comment({"You can change the default localization.", "Currently Available translations:", "- English - en", "- Hungarian - hu", "If you'd like to translate to your language you can do it here:", "https://poeditor.com/join/project/MkiMGNgS4j"})
	public String locale = "en";

	@Name("createTool")
	@Comment({"You don't need to give the players Worldedit permissions.", "The players need to select a region with this tool."})
	public String createTool = Material.WOOD_SPADE.toString();

	@Name("checkTool")
	@Comment({"You don't need to give the players Worldedit permissions.", "The players can check the zones info."})
	public String checkTool = Material.WOOD_SWORD.toString();

	@Name("Delay")
	@Comment({"You can set the delays between checks."})
	public Delay delay;

	public class Delay implements Section
	{
		@Name("checkTool")
		public int checkTool = 5;
	}

	@Name("Zone")
	public Zone zone;

	public class Zone implements Section
	{
		@Name("Regex")
		public String regex = "[^a-zA-Z0-9_\\p{L}+]";
	}

	@Name("Economy")
	@Comment({"Here you can change the default values for the economy part"})
	public Economy economy;

	public class Economy implements Section
	{
		@Name("Enabled")
		public boolean enabled = false;

		@Name("Create")
		public int create = 50;

		@Name("Delete")
		public int delete = 50;

		@Name("Flag")
		public int flag = 50;

		@Name("Expand")
		public int expand = 50;

		@Name("Member")
		public Member member;

		public class Member implements Section
		{
			@Name("Add")
			public int memberAdd = 50;

			@Name("Remove")
			public int memberRemove = 50;
		}

		@Name("Owner")
		public Owner owner;

		public class Owner implements Section
		{
			@Name("Add")
			public int ownerAdd = 50;

			@Name("Remove")
			public int ownerRemove = 50;
		}
	}

	@Name("DisabledWorlds")
	@Comment({"Disabled in these worlds."})
	public List<String> disabledWorlds = Arrays.asList("world_nether", "world_the_end");

	@Name("Messages")
	@Comment({"Enable or disable the option to show the welcome/farewell messages.", "Permission: Whether it require a permission to enable/disable the message."})
	public Messages messages;

	public class Messages implements Section
	{

		@Name("Title")
		public Title title;

		public class Title implements Section
		{
			@Name("Enabled")
			public boolean enabled = true;

			@Name("Permission")
			public boolean permission = false;
		}

		@Name("ActionBar")
		public ActionBar actionBar;

		public class ActionBar implements Section
		{
			@Name("Enabled")
			public boolean enabled = true;

			@Name("Permission")
			public boolean permission = false;
		}

		@Name("BossBar")
		public BossBar bossBar;

		public class BossBar implements Section
		{
			@Name("Enabled")
			public boolean enabled = true;

			@Name("Permission")
			public boolean permission = false;
		}
	}

	@Name("Restriction")
	@Comment({"Example:",
			"For a 25x256x25 zone you have to add this under (50, 256, 50): myzone.zone.max-size.1:",
			"(25, 256, 25): myzone.zone.max-size.2",
			"When you add this permission 'myzone.zone.max-size.2' the player will have a zone restriction of (25, 256, 25).\n",
			"To restrict the max zone of a player to 10 you need to add this under 3: myzone.zone.max-zone.1",
			"10: myzone.zone.max-zone.2",
			"And when you add this permission 'myzone.zone.max-zone.2' the player will have a zone restriction of 10."})
	public Restriction restriction;

	public class Restriction implements Section
	{
		@Name("BlockedFlags")
		public List<String> blockedFlags = Arrays.asList("sleep", "pistons", "exit", "game-mode", "receive-chat", "send-chat");

		@Name("Size")

		public HashMap<String, String> maxZoneSizePermission = new HashMap<String, String>()
		{{
			put("(-1, -1, -1)", "myzone.zone.max-size.unlimited");
			put("(50, 256, 50)", "myzone.zone.max-size.1");
		}};

		@Name("Zone")
		public HashMap<Integer, String> maxZonePermission = new HashMap<Integer, String>()
		{{
			put(-1, "myzone.zone.max-zone.unlimited");
			put(3, "myzone.zone.max-zone.1");
			put(1, "myzone.zone.max-zone.2");
		}};
	}
}
