package hu.tryharddood.myzone.Zones;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;


public class Settings {
	private static final HashMap<UUID, Settings> setts = new HashMap<>();

	private Player   player;
	private Location border1;
	private Location border2;

	private Settings(Player player, Location arg1, Location arg2) {
		this.player = player;
		this.border1 = arg1;
		this.border2 = arg2;
	}

	public static Settings getSett(Player player) {
		if (!setts.containsKey(player.getUniqueId()))
		{
			Settings sett = new Settings(player, null, null);
			setts.put(player.getUniqueId(), sett);
			return sett;
		}
		Settings sett = setts.get(player.getUniqueId());
		sett.setPlayer(player);
		return sett;
	}

	public static String canBuildZone(Player player, Location[] border) {
		if (checkOwnsPermission(player))
		{
			return "max";
		}
		return checkSizePermission(player, border);
	}

	public static String canBuildZone(Player player, Vector[] border) {
		if (checkOwnsPermission(player))
		{
			return "max";
		}
		return checkSizePermission(player, border);
	}

	private static boolean checkOwnsPermission(Player player) {
		Settings sett = Settings.getSett(player);

		return (myZone.worldGuardHelper.getPlayerRegionsNum(player.getUniqueId()) >= sett.getMaxZones()) && (sett.getMaxZones() != -1);
		//return (sett.getPlayerZones() >= sett.getMaxZones()) && (sett.getMaxZones() != -1);
	}

	public static String checkSizePermission(Player player, Object[] border) {
		if (!(border instanceof Vector[]) && !(border instanceof Location[]))
		{
			return "";
		}

		Settings sett = Settings.getSett(player);

		com.sk89q.worldedit.Vector lc1 = border instanceof Vector[] ? toVector((Vector) border[0]) : toVector((Location) border[0]);
		com.sk89q.worldedit.Vector lc2 = border instanceof Vector[] ? toVector((Vector) border[1]) : toVector((Location) border[1]);

		int sizeX = lc2.getBlockX() - lc1.getBlockX();
		int sizeZ = lc2.getBlockZ() - lc1.getBlockZ();
		int sizeY = lc2.getBlockY() - lc1.getBlockY();

		com.sk89q.worldedit.Vector maxSize = sett.getMaxSize();

		/*System.out.println("Compare:\n"
		                   + "sizeX > maxSize.getX(): " + Math.abs(sizeX) + " > " + maxSize.getX() + "\n"
		                   + "sizeY > maxSize.getY(): " + Math.abs(sizeY) + " > " + maxSize.getY() + "\n"
		                   + "sizeZ > maxSize.getZ(): " + Math.abs(sizeZ) + " > " + maxSize.getZ() + "\n");
*/
		if ((Math.abs(sizeX) > maxSize.getX() && maxSize.getX() != -1.0D)
		    || (Math.abs(sizeY) > maxSize.getY() && maxSize.getY() != -1.0D)
		    || (Math.abs(sizeZ) > maxSize.getZ() && maxSize.getZ() != -1.0D))
		{
			return "size:(" + sizeX + ", " + sizeY + ", " + sizeZ + ")";
		}
		return "";
	}

	public Player getPlayer() {
		return this.player;
	}

	private void setPlayer(Player a) {
		this.player = a;
	}

	public Location getBorder1() {
		return this.border1;
	}

	public Location getBorder2() {
		return this.border2;
	}

	public void setBorder(int a, Location b) {
		if (a == 1)
		{
			this.border1 = b;
		}
		else if (a == 2)
		{
			this.border2 = b;
		}
	}

	public Integer getPlayerZones() {
		RegionManager regionManager;
		Integer       number = 0;
		for (World world : Bukkit.getWorlds())
		{
			regionManager = myZone.worldGuardReflection.getWorldGuardPlugin().getRegionManager(world);
			for (Map.Entry<String, ProtectedRegion> object : regionManager.getRegions().entrySet())
			{
				if (object.getValue().getOwners().contains(getPlayer().getUniqueId()) || object.getValue().getOwners().contains(getPlayer().getName()))
				{
					number++;
				}
			}
		}
		return number;
	}

	public int getMaxZones() {
		int max = -1;

		for (String perm : Properties.getMaxZonePermissions().keySet())
		{
			if (getPlayer().hasPermission(perm))
			{
				int size = Properties.getMaxZonePermissions().get(perm);
				if ((size != -1) && (size <= max))
				{
					continue;
				}
				max = size;
			}
		}
		return max;
	}

	public com.sk89q.worldedit.Vector getMaxSize() {
		com.sk89q.worldedit.Vector max = toVector(new Vector(-1, -1, -1));

		for (String perm : Properties.getMaxZoneSizePermissions().keySet())
		{
			if (getPlayer().hasPermission(perm))
			{
				max = Properties.getMaxZoneSizePermissions().get(perm);
			}
		}
		return max;
	}
}
