package hu.tryharddood.myzone;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import hu.tryharddood.mcversion.MCVersion;
import hu.tryharddood.myzone.Commands.CommandHandler;
import hu.tryharddood.myzone.Commands.MainCommand;
import hu.tryharddood.myzone.Commands.SubCommands.*;
import hu.tryharddood.myzone.Listeners.pListener;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryListener;
import hu.tryharddood.myzone.Util.Localization.I18n;
import hu.tryharddood.myzone.Util.MessagesAPI;
import hu.tryharddood.myzone.Util.Updater;
import hu.tryharddood.myzone.Util.WorldGuard.WorldGuardHelper;
import hu.tryharddood.myzone.Util.WorldGuard.WorldGuardReflection;
import hu.tryharddood.myzone.Zones.ZoneManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class myZone extends JavaPlugin {

	public static myZone               myZonePlugin;
	public static Economy              vaultEcon;
	public static WorldGuardReflection worldGuardReflection;
	public static WorldGuardHelper     worldGuardHelper;
	public static MCVersion.Version    serverVersion;
	public static ZoneManager          zoneManager;
	private static I18n   _i18n;
	private static String _name;
	private static String _version;
	public InventoryListener inventoryListener;

	public static void log(String message) {
		Bukkit.getConsoleSender().sendMessage("[" + _name + " v" + _version + "] " + message);
	}

	public I18n getI18n() {
		return _i18n;
	}

	@Override
	public void onEnable() {
		myZonePlugin = this;
		serverVersion = MCVersion.Version.getVersion();

		_name = getDescription().getName();
		_version = getDescription().getVersion();

		if (serverVersion.olderThan(MCVersion.Version.v1_8_R1))
		{
			log(ChatColor.RED + "You server version is not supported. Please update your server...");
			log(ChatColor.RED + "Disabling " + _name + " " + _version);
			return;
		}

		log("Starting...");

		log("Trying to hook WorldGuard...");
		setWgPlugin();

		_i18n = new I18n(this);
		_i18n.onEnable();

		MessagesAPI _messagesAPI = new MessagesAPI();
		_messagesAPI.onEnable();

		log("Creating commands...");
		registerCommands();

		log("Hooking events...");
		registerEvents();

		log("Loading configuration file...");
		Properties.loadConfiguration();

		log("Checking for updates...");
		Updater              updater = new Updater(this, 254781, this.getFile(), Updater.UpdateType.DEFAULT, false);
		Updater.UpdateResult result  = updater.getResult();
		switch (result)
		{
			case DISABLED:
				log(ChatColor.RED + "Update checking is disabled.");
				break;
			case SUCCESS:
				log(ChatColor.GREEN + "Downloaded the latest version. Please restart the server.");
				break;
			case NO_UPDATE:
				log(ChatColor.GREEN + "You are using the latest version of " + this.getDescription().getName());
				break;
			case FAIL_DOWNLOAD:
				log(ChatColor.GREEN + "New version found but couldn't download it.");
				break;
			case UPDATE_AVAILABLE:
				log(ChatColor.GREEN + "There's a new version available.");
		}

		zoneManager = new ZoneManager();
		zoneManager.loadZones();
	}

	@Override
	public void onDisable() {

		zoneManager.saveZones();
		worldGuardHelper.saveAll();
	}

	private void registerEvents() {
		this.getServer().getPluginManager().registerEvents(inventoryListener = new InventoryListener(this), this);
		this.getServer().getPluginManager().registerEvents(new pListener(), this);

		log("Events successfully hooked.");
	}

	private void registerCommands() {
		getCommand("myzone").setExecutor(new CommandHandler());

		CommandHandler.addComand(Collections.singletonList("create"), new CreateCommand());
		CommandHandler.addComand(Collections.singletonList("delete"), new DeleteCommand());
		CommandHandler.addComand(Collections.singletonList("members"), new MembersCommand());
		CommandHandler.addComand(Collections.singletonList("flag"), new FlagCommand());
		CommandHandler.addComand(Collections.singletonList("reload"), new ReloadCommand());
		CommandHandler.addComand(Collections.singletonList("help"), new HelpCommand());
		CommandHandler.addComand(Collections.singletonList("expand"), new ExpandCommand());
		CommandHandler.addComand(Collections.singletonList("info"), new InfoCommand());
		CommandHandler.addComand(Collections.singletonList("version"), new VersionCommand());
		CommandHandler.addComand(Collections.singletonList("setpos"), new SetPosCommand());
		CommandHandler.addComand(Collections.singletonList("list"), new ListCommand());
		CommandHandler.addComand(Collections.singletonList("owners"), new OwnersCommand());
		CommandHandler.addComand(Collections.singletonList("visualize"), new VisualizeCommand());
		CommandHandler.addComand(Collections.singletonList("gui"), new MainCommand());

		log("Commands successfully created.");
	}

	private void setWgPlugin() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
		{
			log("Couldn't hook WorldGuard. Maybe not installed?");
			log("In order to run this plugin you must have WorldGuard installed. Stopping...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		log("WorldGuard (v" + plugin.getDescription().getVersion() + ") successfully hooked.");

		worldGuardReflection = new WorldGuardReflection((WorldGuardPlugin) plugin);
		worldGuardHelper = new WorldGuardHelper(this);
	}
}
