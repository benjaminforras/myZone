package hu.tryharddood.myzone.Zones;

import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.myZone;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/*****************************************************
 * Created by TryHardDood on 2016. 07. 07..
 ****************************************************/
public class ZoneManager {

	private final JavaPlugin                  _instance = JavaPlugin.getProvidingPlugin(ZoneManager.class);
	private       HashMap<String, ZoneObject> _zones    = new HashMap<>();

	public ZoneManager() {}

	public void loadZones() {
		_zones.clear();

		File zonesFolder = new File(_instance.getDataFolder() + File.separator + "zones");
		if (!zonesFolder.exists())
		{
			zonesFolder.mkdirs();
		}

		if (!zonesFolder.isDirectory())
		{
			return;
		}

		File[] zonesFiles = zonesFolder.listFiles();
		if (zonesFiles.length == 0)
		{
			return;
		}

		String  fileName;
		Integer extensionIndex;

		for (File zoneFile : zonesFiles)
		{
			if (!zoneFile.isFile()) continue;

			fileName = zoneFile.getName();
			extensionIndex = fileName.lastIndexOf(".");
			if (extensionIndex > 0)
			{
				fileName = fileName.substring(0, extensionIndex);
			}

			if (Properties.getRegex().matcher(fileName).find())
			{
				_instance.getLogger().info("Error when trying to load " + fileName);
				_instance.getLogger().info("- The name contains special characters.");
				continue;
			}

			getZoneData(zoneFile);
		}

		myZone.log("Successfully loaded " + _zones.size() + " zones");
	}

	private void getZoneData(File file) {
		YamlConfiguration zoneConfiguration = YamlConfiguration.loadConfiguration(file);
		ZoneObject        zoneObject        = new ZoneObject();

		if (zoneConfiguration.getString("OwnerID") == null || zoneConfiguration.getString("ZoneName") == null || zoneConfiguration.getString("RegionID") == null)
		{
			_instance.getLogger().log(Level.WARNING, "Can't load " + file.getName() + ". Wrong file setup!");
			return;
		}

		UUID   ownerID  = UUID.fromString(zoneConfiguration.getString("OwnerID"));
		String zoneName = zoneConfiguration.getString("ZoneName");
		String regionID = zoneConfiguration.getString("RegionID");

		zoneObject.setOwnerID(ownerID);
		zoneObject.setZoneName(zoneName);
		zoneObject.setRegionID(regionID);

		_zones.put(zoneName, zoneObject);
	}

	public HashMap<String, ZoneObject> getZones() {
		return _zones;
	}

	public void saveZones() {
		for (Map.Entry<String, ZoneObject> zoneEntry : _zones.entrySet())
		{
			try
			{
				zoneEntry.getValue().updateFile();
			} catch (IOException e)
			{
				//e.printStackTrace();
				System.out.println("Error saving " + zoneEntry.getValue().getZoneName());
			}
		}
	}

	/**
	 * @param arg1 Zone's name
	 * @return ZoneObject(Owner UUID, Zone name, WG ID)
	 */
	public ZoneObject getZoneObject(String arg1) {
		if (_zones.containsKey(arg1))
		{
			return _zones.get(arg1);
		}
		return null;
	}

	/**
	 * @param arg1 Zone's name
	 * @return WG's region ID
	 */
	public String getRegionID(String arg1) {
		if (getZoneObject(arg1) != null)
		{ return getZoneObject(arg1).getRegionID(); }
		return null;
	}

	/**
	 * @param arg1 Zone's Name
	 * @return UUID of the Owner
	 */
	public UUID getRegionOwner(String arg1) {
		if (getZoneObject(arg1) != null)
		{ return getZoneObject(arg1).getOwnerID(); }
		return null;
	}

	/**
	 * @param arg1 WG's region ID
	 * @return Zone's Name
	 */
	public String getRegionName(String arg1) {
		for (Map.Entry<String, ZoneObject> zoneEntry : _zones.entrySet())
		{
			if (zoneEntry.getValue().getRegionID().equals(arg1))
			{
				return zoneEntry.getKey();
			}
		}
		return null;
	}

	/**
	 * @param arg1 WG's region ID
	 * @return ZoneObject
	 */
	public ZoneObject getRegion(String arg1) {
		for (Map.Entry<String, ZoneObject> zoneEntry : _zones.entrySet())
		{
			if (zoneEntry.getValue().getRegionID().equals(arg1))
			{
				return zoneEntry.getValue();
			}
		}
		return null;
	}
}
