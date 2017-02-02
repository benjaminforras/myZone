/*
 * Copyright 2015-2016 Benjamin Forrás <TryHardDood>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 	1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 * 	2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

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

public class ZoneUtils {

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

		int sizeX = Math.abs(Math.abs(lc2.getBlockX()) - Math.abs(lc1.getBlockX()));
		int sizeZ = Math.abs(Math.abs(lc2.getBlockY()) - Math.abs(lc1.getBlockY()));
		int sizeY = Math.abs(Math.abs(lc2.getBlockZ()) - Math.abs(lc1.getBlockZ()));

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
