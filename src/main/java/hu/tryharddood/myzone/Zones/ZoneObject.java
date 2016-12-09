package hu.tryharddood.myzone.Zones;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/*****************************************************
 * Created by TryHardDood on 2016. 07. 07..
 ****************************************************/
public class ZoneObject {

	private transient final JavaPlugin _instance = JavaPlugin.getProvidingPlugin(ZoneObject.class);

	private String zoneName;
	private UUID   ownerID;
	private String regionID;

	public ZoneObject() {

	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public UUID getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(UUID ownerID) {
		this.ownerID = ownerID;
	}

	public String getRegionID() {
		return regionID;
	}

	public void setRegionID(String regionID) {
		this.regionID = regionID;
	}

	private File getFile() {
		File zonesFolder = new File(_instance.getDataFolder() + File.separator + "zones");
		if (!zonesFolder.exists())
		{
			zonesFolder.mkdirs();
		}

		return new File(zonesFolder, getZoneName() + ".yml");
	}

	public void createFile() throws IOException {
		File file = getFile();
		if (file.exists())
		{
			updateFile();
		}
		else
		{
			file.createNewFile();
			updateFile();
		}
	}

	public void deleteFile() {
		getFile().delete();
	}

	public void updateFile() throws IOException {
		YamlConfiguration zoneConfiguration = YamlConfiguration.loadConfiguration(getFile());
		zoneConfiguration.set("OwnerID", getOwnerID().toString());
		zoneConfiguration.set("ZoneName", getZoneName());
		zoneConfiguration.set("RegionID", getRegionID());

		zoneConfiguration.save(getFile());
	}
}
