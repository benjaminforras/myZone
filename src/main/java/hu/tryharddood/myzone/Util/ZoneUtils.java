package hu.tryharddood.myzone.Util;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;


public class Settings {

	public static String canBuildZone(Player player, com.sk89q.worldedit.Vector[] border) {
		if (checkOwnsPermission(player))
		{
			return "max";
		}
		return checkSizePermission(player, border);
	}

	private static boolean checkOwnsPermission(Player player) {
		return (myZone.worldGuardHelper.getPlayerRegionsNum(player.getUniqueId()) >= getMaxZones(player)) && (getMaxZones(player) != -1);
	}

	public static String checkSizePermission(Player player, com.sk89q.worldedit.Vector[] border) {
		if (border == null)
		{
			return "";
		}

		com.sk89q.worldedit.Vector lc1 = border[0];
		com.sk89q.worldedit.Vector lc2 = border[1];

		int sizeX = lc2.getBlockX() - lc1.getBlockX();
		int sizeZ = lc2.getBlockZ() - lc1.getBlockZ();
		int sizeY = lc2.getBlockY() - lc1.getBlockY();

		com.sk89q.worldedit.Vector maxSize = getMaxSize(player);

		if ((Math.abs(sizeX) > maxSize.getX() && maxSize.getX() != -1.0D) || (Math.abs(sizeY) > maxSize.getY() && maxSize.getY() != -1.0D) || (Math.abs(sizeZ) > maxSize.getZ() && maxSize.getZ() != -1.0D))
		{
			return "size:(" + sizeX + ", " + sizeY + ", " + sizeZ + ")";
		}
		return "";
	}

	public static Integer getPlayerZones(Player player) {
		RegionManager regionManager;
		Integer       number = 0;
		for (World world : Bukkit.getWorlds())
		{
			regionManager = myZone.worldGuardReflection.getWorldGuardPlugin().getRegionManager(world);
			for (Map.Entry<String, ProtectedRegion> object : regionManager.getRegions().entrySet())
			{
				if (object.getValue().getOwners().contains(player.getUniqueId()) || object.getValue().getOwners().contains(player.getName()))
				{
					number++;
				}
			}
		}
		return number;
	}

	public static int getMaxZones(Player player) {
		int max = -1;

		for (String perm : Properties.getMaxZonePermissions().keySet())
		{
			if (player.hasPermission(perm))
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

	public static com.sk89q.worldedit.Vector getMaxSize(Player player) {
		com.sk89q.worldedit.Vector max = toVector(new Vector(-1, -1, -1));

		for (String perm : Properties.getMaxZoneSizePermissions().keySet())
		{
			if (player.hasPermission(perm))
			{
				max = Properties.getMaxZoneSizePermissions().get(perm);
			}
		}
		return max;
	}
}
