package hu.tryharddood.myzone.Util;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Zones.Settings;
import hu.tryharddood.myzone.Zones.ZoneObject;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class WGWrapper {
	private static final JavaPlugin   plugin   = JavaPlugin.getProvidingPlugin(WGWrapper.class);
	private static       FlagRegistry registry = myZone.getWgPlugin().getFlagRegistry();

	public static boolean deleteRegion(ProtectedRegion regionName) {
		RegionManager manager = null;

		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			for (Map.Entry<String, ProtectedRegion> s : regionManager.getRegions().entrySet())
			{
				if (s.getValue().equals(regionName))
				{
					manager = regionManager;
				}
			}
		}

		String regionID = regionName.getId();
		if (!manager.hasRegion(regionID))
		{
			plugin.getLogger().warning("Manager doesn't contain this zone.");
			return false;
		}

		manager.removeRegion(regionName.getId());

		String zonename = myZone.getZoneManager().getRegionName(regionID);

		ZoneObject zoneObject = myZone.getZoneManager().getZoneObject(zonename);
		zoneObject.deleteFile();
		myZone.getZoneManager().getZones().remove(zonename);

		try
		{
			manager.save();
		} catch (StorageException e)
		{
			plugin.getLogger().warning("Failed to delete region: " + e.getMessage());
		}
		return true;
	}

	public static void createRegion(String zoneName, String regionID, Vector[] vectors, Player owner, World world) {
		ProtectedRegion region = new ProtectedCuboidRegion(regionID, vectors[0].toBlockVector(), vectors[1].toBlockVector());

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner.getUniqueId());
		LocalPlayer   lcPlayer      = myZone.getWgPlugin().wrapOfflinePlayer(offlinePlayer);
		region.getOwners().addPlayer(lcPlayer);

		ZoneObject zoneObject = new ZoneObject();
		zoneObject.setRegionID(regionID);
		zoneObject.setOwnerID(owner.getUniqueId());
		zoneObject.setZoneName(zoneName);

		for (Map.Entry<String, ProtectedRegion> region1 : myZone.getWgPlugin().getRegionManager(world).getRegions().entrySet())
		{
			if (region1.getValue().getId().equalsIgnoreCase(zoneName))
			{
				owner.sendMessage(tl("Error") + " " + tl("CreateZone_Error3", zoneName));
				return;
			}

			if (!Objects.equals(region1.getValue().getId(), region.getId()))
			{
				if (WGWrapper.regionsIntersect(region1.getValue(), region))
				{
					owner.sendMessage(tl("Error") + " " + tl("CreateZone_Error4"));
					return;
				}
			}
		}

		try
		{
			zoneObject.createFile();

			myZone.getWgPlugin().getRegionManager(world).addRegion(region);
			myZone.getWgPlugin().getRegionManager(world).save();
		} catch (IOException | StorageException e)
		{
			plugin.getLogger().warning("Failed to delete region: " + e.getMessage());
		}
		myZone.getZoneManager().getZones().put(zoneName, zoneObject);
		owner.sendMessage(tl("Success") + " " + tl("CreateZone_Success", zoneName));
	}

	public static Integer getPlayerRegionsNum(UUID player) {
		int num = 0;
		for (Map.Entry<String, ZoneObject> zoneEntry : myZone.getZoneManager().getZones().entrySet())
		{
			if (zoneEntry.getValue().getOwnerID().equals(player))
			{ num++; }
		}

		return num;
	}

	public static ArrayList<String> getPlayerOwnedRegions(UUID player) {
		ArrayList<String> zones = new ArrayList<>();
		for (Map.Entry<String, ZoneObject> zoneEntry : myZone.getZoneManager().getZones().entrySet())
		{
			if (zoneEntry.getValue().getOwnerID().equals(player))
				zones.add(zoneEntry.getKey());
		}

		return zones;
	}

	public static ArrayList<String> getPlayerMemberRegions(UUID player) {
		ArrayList<String> zones = new ArrayList<>();
		ProtectedRegion   region;
		for (Map.Entry<String, ZoneObject> zoneEntry : myZone.getZoneManager().getZones().entrySet())
		{
			region = WGWrapper.getRegion(zoneEntry.getValue().getRegionID());
			if (region == null)
				continue;
			if (region.getMembers() == null || !region.getMembers().contains(player))
				continue;

			zones.add(zoneEntry.getKey());
		}

		return zones;
	}

	public static HashMap<String, List<String>> getPlayerRegions(UUID player) {
		HashMap<String, List<String>> zones = new HashMap<>();

		ArrayList<String> owned  = new ArrayList<>();
		ArrayList<String> member = new ArrayList<>();

		ProtectedRegion region;
		for (Map.Entry<String, ZoneObject> zoneEntry : myZone.getZoneManager().getZones().entrySet())
		{
			region = WGWrapper.getRegion(zoneEntry.getValue().getRegionID());
			if (region == null)
				continue;

			if (region.getOwners().contains(player))
			{
				owned.add(zoneEntry.getKey());
			}
			else if (region.getMembers().contains(player) && !region.getOwners().contains(player))
			{
				member.add(zoneEntry.getKey());
			}
		}
		zones.put("owned", owned);
		zones.put("member", member);
		return zones;
	}

	public static int expandRegion(Player player, ProtectedRegion existing, BlockFace dir, int expansion) {
		if (expansion <= 0)
		{
			return -1;
		}

		RegionManager manager = null;
		World         world   = null;
		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			for (Map.Entry<String, ProtectedRegion> s : regionManager.getRegions().entrySet())
			{
				if (s.getValue().equals(existing))
				{
					world = Bukkit.getWorld(regionManager.getName());
					manager = regionManager;
				}
			}
		}

		BlockVector min = existing.getMinimumPoint();
		BlockVector max = existing.getMaximumPoint();
		int         x1  = min.getBlockX();
		int         y1  = min.getBlockY() <= 0 ? 0 : min.getBlockY();
		int         z1  = min.getBlockZ();
		int         x2  = max.getBlockX();
		int         y2  = max.getBlockY() >= world.getMaxHeight() ? world.getMaxHeight() : max.getBlockY();
		int         z2  = max.getBlockZ();

		if (dir == BlockFace.UP)
		{
			y2 = y2 + expansion;
			if (y2 > world.getMaxHeight())
			{
				return -2;
			}
		}
		else if (dir == BlockFace.DOWN)
		{
			y1 = y1 - expansion;
			if (y1 < 0)
			{
				return -3;
			}
		}
		else if (dir == BlockFace.NORTH)
		{
			z1 = z1 - expansion;
		}
		else if (dir == BlockFace.SOUTH)
		{
			z2 = z2 + expansion;
		}
		else if (dir == BlockFace.EAST)
		{
			x2 = x2 + expansion;
		}
		else if (dir == BlockFace.WEST)
		{
			x1 = x1 - expansion;
		}
		else
		{
			return -1;
		}

		BlockVector newMin = new BlockVector(x1, y1, z1);
		BlockVector newMax = new BlockVector(x2, y2, z2);

		String permission = Settings.checkSizePermission(player, new Location[]{new Location(world, x1, y1, z1), new Location(world, x2, y2, z2)});
		if (permission.startsWith("size"))
		{
			Vector maxSize = Settings.getSett(player).getMaxSize();
			player.sendMessage(tl("Error") + " " + tl("ZoneTooBig", permission.split(":")[1], maxSize.getX() + ", " + maxSize.getY() + ", " + maxSize.getZ()));
			return -1;
		}

		int area = (x2 - x1) * (z2 - z1);

		ProtectedRegion newRegion = new ProtectedCuboidRegion(existing.getId(), newMin, newMax);

		for (ProtectedRegion region1 : WGWrapper.getRegions())
		{
			if (!Objects.equals(region1.getId(), newRegion.getId()))
			{
				if (regionsIntersect(region1, newRegion))
				{
					player.sendMessage(tl("Error") + " " + tl("CreateZone_Error4"));
					return -1;
				}
			}
		}


		newRegion.setMembers(existing.getMembers());
		newRegion.setOwners(existing.getOwners());
		newRegion.setFlags(existing.getFlags());
		newRegion.setPriority(existing.getPriority());
		try
		{
			newRegion.setParent(existing.getParent());
		} catch (ProtectedRegion.CircularInheritanceException ignore)
		{
		}

		manager.addRegion(newRegion);

		try
		{
			manager.save();
		} catch (StorageException e)
		{
			plugin.getLogger().warning("Failed to write region: " + e.getMessage());
		}
		return area;
	}

	private static boolean regionsIntersect(ProtectedRegion a, ProtectedRegion b) {
		return intersects(a.getMinimumPoint().getBlockX(), a.getMaximumPoint().getBlockX(), b.getMinimumPoint().getBlockX(), b.getMaximumPoint().getBlockX()) && intersects(a.getMinimumPoint().getBlockY(), a.getMaximumPoint().getBlockY(), b.getMinimumPoint().getBlockY(), b.getMaximumPoint().getBlockY()) && intersects(a.getMinimumPoint().getBlockZ(), a.getMaximumPoint().getBlockZ(), b.getMinimumPoint().getBlockZ(), b.getMaximumPoint().getBlockZ());
	}

	private static boolean intersects(int aMin, int aMax, int bMin, int bMax) {
		return aMin <= bMax && aMax >= bMin;
	}

	public static ProtectedRegion getRegion(String id) {
		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			if (regionManager.getRegion(id) != null)
			{
				return regionManager.getRegion(id);
			}
		}
		return null;
	}

	public static List<ProtectedRegion> getRegions() {
		ArrayList<ProtectedRegion> regions = new ArrayList<>();
		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			regions.addAll(regionManager.getRegions().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
		}
		return regions;
	}

	public static RegionManager getRegionManager(String regionid) {
		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			for (Map.Entry<String, ProtectedRegion> regionEntry : regionManager.getRegions().entrySet())
			{
				if (regionEntry.getValue().getId().equalsIgnoreCase(regionid))
					return regionManager;
			}
		}
		return null;
	}

	public static void saveAll() {
		for (RegionManager regionManager : myZone.getWgPlugin().getRegionContainer().getLoaded())
		{
			try
			{
				regionManager.save();
			} catch (StorageException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static Flag<?> fuzzyMatchFlag(String id)
	{
		return DefaultFlag.fuzzyMatchFlag(registry, id);
	}
}
