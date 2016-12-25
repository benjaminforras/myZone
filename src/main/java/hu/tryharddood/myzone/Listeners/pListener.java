package hu.tryharddood.myzone.Listeners;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import hu.tryharddood.mcversion.MCVersion;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Settings;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class pListener implements Listener {
	private final Map<UUID, Long> spamBlock = new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		Player player = event.getPlayer();
		Action action = event.getAction();

		if (myZone.getVersion().newerThan(MCVersion.Version.v1_9_R1))
		{
			if (event.getHand() == null)
			{
				return;
			}

			EquipmentSlot slot = event.getHand();
			if (slot.equals(EquipmentSlot.HAND))
			{
				if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
				{
					return;
				}

				ItemStack itemStack = player.getInventory().getItemInMainHand();
				_handleClickEvent(player, itemStack, event.getClickedBlock().getLocation(), action, event);
			}
		}
		else
		{
			if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
			{
				return;
			}

			ItemStack itemStack = player.getItemInHand();
			_handleClickEvent(player, itemStack, event.getClickedBlock().getLocation(), action, event);
		}
	}

	private void _handleClickEvent(Player player, ItemStack itemStack, Location location, Action action, PlayerInteractEvent event) {
		if (action == Action.LEFT_CLICK_BLOCK)
		{
			if (itemStack.getType().equals(Properties.getCreateTool()))
			{
				if (!player.hasPermission(Variables.PlayerInteract.SELECTPERMISSION))
				{
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}

				if (Properties.getDisabledWorlds().contains(location.getWorld().getName()))
				{
					player.sendMessage(tl("Error") + " " + tl("Creation_Select_Error1", location.getWorld().getName()));
					event.setCancelled(true);
					return;
				}
				Settings settings = Settings.getSett(player);
				settings.setBorder(1, location);

				player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "1."));
				event.setCancelled(true);
			}
		}
		else if (action == Action.RIGHT_CLICK_BLOCK)
		{
			if (itemStack.getType().equals(Properties.getCreateTool()))
			{
				if (!player.hasPermission(Variables.PlayerInteract.SELECTPERMISSION))
				{
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}

				if (Properties.getDisabledWorlds().contains(location.getWorld().getName()))
				{
					player.sendMessage(tl("Error") + " " + tl("Creation_Select_Error1", location.getWorld().getName()));
					event.setCancelled(true);
					return;
				}
				Settings settings = Settings.getSett(player);
				settings.setBorder(2, location);

				player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "2."));
				event.setCancelled(true);
			}
			else if (itemStack.getType().equals(Properties.getCheckTool()))
			{
				if (!player.hasPermission(Variables.PlayerInteract.CHECKPERMISSION))
				{
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}
				if (spamBlock.containsKey(player.getUniqueId()))
				{
					if (spamBlock.get(player.getUniqueId()) > System.currentTimeMillis())
					{
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_SpamBlock", ((spamBlock.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000) + " " + tl("Text_Second", true)));
						event.setCancelled(true);
						return;
					}
				}

				ApplicableRegionSet set = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location);

				if (set.getRegions().size() == 0)
				{
					player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
					spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (Properties.getCheckToolDelay() * 1000));
					event.setCancelled(true);
					return;
				}

				set.getRegions().stream().forEach(region ->
				{
					if (region.getId().equalsIgnoreCase("__global__"))
					{
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
						spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (Properties.getCheckToolDelay() * 1000));
						event.setCancelled(true);
						return;
					}

					if (region == null)
					{
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
						spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (Properties.getCheckToolDelay() * 1000));
						event.setCancelled(true);
						return;
					}

					player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.GOLD + myZone.getZoneManager().getRegionName(region.getId()));
					player.sendMessage(ChatColor.GRAY + tl("Text_Owners", true) + ": ");
					if (region.getOwners().getUniqueIds().size() == 0)
					{
						player.sendMessage(ChatColor.GOLD + "- " + "None");
					}
					else
					{
						for (UUID s : region.getOwners().getUniqueIds())
						{
							player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
						}
					}
					player.sendMessage(ChatColor.GRAY + tl("Text_Location", true) + ": " + ChatColor.GOLD + location.getWorld().getName() + " - " + (region.getMinimumPoint() + " -> " + region.getMaximumPoint()));
					player.sendMessage(ChatColor.GRAY + tl("Text_Members", true) + ": ");
					if (region.getMembers().getUniqueIds().size() == 0)
					{
						player.sendMessage(ChatColor.GOLD + "- " + "None");
					}
					else
					{
						for (UUID s : region.getMembers().getUniqueIds())
						{
							player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
						}
					}

					String temp = "";
					for (Flag flag : region.getFlags().keySet())
					{
						temp += flag.getName() + ", ";
					}
					if (!Objects.equals(temp, ""))
					{
						temp = temp.substring(0, temp.length() - 2);
					}
					player.sendMessage(ChatColor.GRAY + tl("Text_Flags", true) + ": " + ChatColor.GOLD + (temp));
					player.sendMessage(ChatColor.GRAY + tl("Text_Type", true) + ": " + ChatColor.GOLD + (region.getType().getName()));
					spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (Properties.getCheckToolDelay() * 1000));
					event.setCancelled(true);
				});
			}
		}
	}
}
