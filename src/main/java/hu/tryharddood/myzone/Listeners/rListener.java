package hu.tryharddood.myzone.Listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class rListener implements Listener {
	private WorldGuardPlugin                  wgPlugin;
	private JavaPlugin                        plugin;
	private Map<Player, Set<ProtectedRegion>> playerRegions;

	public rListener(JavaPlugin plugin, WorldGuardPlugin wgPlugin) {
		this.plugin = plugin;
		this.wgPlugin = wgPlugin;

		this.playerRegions = new HashMap<>();
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		this.playerRegions.remove(e.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.playerRegions.remove(e.getPlayer());
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		e.setCancelled(updateRegions(e.getPlayer(), e.getTo()));
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		e.setCancelled(updateRegions(e.getPlayer(), e.getTo()));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		updateRegions(e.getPlayer(), e.getPlayer().getLocation());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		updateRegions(e.getPlayer(), e.getRespawnLocation());
	}

	private synchronized boolean updateRegions(final Player player, Location to) {
		Set<ProtectedRegion> regions;
		if (this.playerRegions.get(player) == null)
		{
			regions = new HashSet<>();
		}
		else
		{
			regions = this.playerRegions.get(player);
		}

		RegionManager rm = this.wgPlugin.getRegionManager(to.getWorld());
		if (rm == null)
		{
			return false;
		}

		ApplicableRegionSet appRegions = rm.getApplicableRegions(to);
		for (final ProtectedRegion region : appRegions)
		{
			if (!regions.contains(region))
			{
				Bukkit.getScheduler().runTaskLater(this.plugin, () ->
				{

					if (region.getId().equalsIgnoreCase("__global__"))
					{
						return;
					}

					String greetmessage = region.getFlag(DefaultFlag.GREET_MESSAGE);
					if (greetmessage != null)
					{
						Title.sendTitle(player, "", ChatColor.translateAlternateColorCodes('&', greetmessage));
					}
				}, 1L);
				regions.add(region);
			}
		}

		Set<ProtectedRegion> app = appRegions.getRegions();
		Iterator             itr = regions.iterator();
		while (itr.hasNext())
		{
			final ProtectedRegion region = (ProtectedRegion) itr.next();
			if (!app.contains(region))
			{
				if (rm.getRegion(region.getId()) != region)
				{
					itr.remove();
				}
				else
				{
					Bukkit.getScheduler().runTaskLater(this.plugin, () ->
					{

						if (region.getId().equalsIgnoreCase("__global__"))
						{
							return;
						}

						String farewellmessage = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
						if (farewellmessage != null)
						{
							Title.sendTitle(player, "", ChatColor.translateAlternateColorCodes('&', farewellmessage));
						}
					}, 1L);
					itr.remove();
				}
			}
		}
		this.playerRegions.put(player, regions);
		return false;
	}
}