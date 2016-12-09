package hu.tryharddood.myzone;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import hu.tryharddood.myzone.Commands.CommandHandler;
import hu.tryharddood.myzone.Commands.MainCommand;
import hu.tryharddood.myzone.Commands.SubCommands.*;
import hu.tryharddood.myzone.Listeners.pListener;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryListener;
import hu.tryharddood.myzone.Util.I18n;
import hu.tryharddood.myzone.Util.Updater;
import hu.tryharddood.myzone.Util.WGWrapper;
import hu.tryharddood.myzone.Zones.ZoneManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class myZone extends JavaPlugin {

	private static WorldGuardPlugin _wgPlugin;
	private static Economy          _econ;

	private static Minecraft.Version _serverversion;

	private static myZone _myZonePlugin;

	private static I18n _i18n;

	private static ZoneManager _zoneManager;

	private static String _name;
	private static String _version;
	public InventoryListener inventoryListener;
	private pListener _plistener;

	public static Minecraft.Version getVersion() {
		return _serverversion;
	}

	public static myZone getInstance() {
		return _myZonePlugin;
	}

	public static WorldGuardPlugin getWgPlugin() {
		return _wgPlugin;
	}

	public static Economy getEconomy() {
		return _econ;
	}

	public static void setEconomy(Economy economy) {
		_econ = economy;
	}

	public static ZoneManager getZoneManager() {
		return _zoneManager;
	}

	public static void log(String message) {
		Bukkit.getConsoleSender().sendMessage("[" + _name + " v" + _version + "] " + message);
	}

	public I18n getI18n() {
		return _i18n;
	}

	@Override
	public void onEnable() {
		_myZonePlugin = this;

		_name = getDescription().getName();
		_version = getDescription().getVersion();

		log("Starting...");

		_serverversion = Minecraft.Version.getVersion();

		log("Trying to hook WorldGuard...");
		setWgPlugin();

		_i18n = new I18n(this);
		_i18n.onEnable();

		log("Creating commands...");
		registerCommands();

		log("Hooking events...");
		registerEvents();

		log("Loading configuration file...");
		Properties.loadConfiguration();

		Updater updater = new Updater(this, 254781, this.getFile(), Updater.UpdateType.DEFAULT, false);
		Updater.UpdateResult result = updater.getResult();
		switch(result)
		{
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

		_zoneManager = new ZoneManager();
		_zoneManager.loadZones();
	}

	@Override
	public void onDisable() {
		WGWrapper.saveAll();
		_plistener = null;
	}

	private void registerEvents() {
		this.getServer().getPluginManager().registerEvents(inventoryListener = new InventoryListener(this), this);

		_plistener = new pListener();
		this.getServer().getPluginManager().registerEvents(_plistener, this);

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
		_wgPlugin = (WorldGuardPlugin) plugin;
	}
}
