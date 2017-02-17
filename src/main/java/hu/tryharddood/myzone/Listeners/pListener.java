package hu.tryharddood.myzone.Listeners;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.myzone.Util.ZoneUtils;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.Zones.Selection;
import hu.tryharddood.myzone.myZone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class pListener implements Listener
{
	private final Map<UUID, Long> spamBlock = new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{

		Player player = event.getPlayer();
		Action action = event.getAction();

		if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
			if (event.getHand() == null) {
				return;
			}

			EquipmentSlot slot = event.getHand();
			if (slot.equals(EquipmentSlot.HAND)) {
				if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) {
					return;
				}

				ItemStack itemStack = player.getInventory().getItemInMainHand();
				_handleClickEvent(player, itemStack, event.getClickedBlock().getLocation(), action, event);
			}
		}
		else {
			if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) {
				return;
			}

			ItemStack itemStack = player.getItemInHand();
			_handleClickEvent(player, itemStack, event.getClickedBlock().getLocation(), action, event);
		}
	}

	private void _handleClickEvent(Player player, ItemStack itemStack, Location location, Action action, PlayerInteractEvent event)
	{
		Selection selection = Selection.getSelection(player);
		int       setPos    = -1;

		if (action == Action.LEFT_CLICK_BLOCK) {
			if (itemStack.getType().equals(myZone.config.createToolMaterial)) {
				if (!player.hasPermission(Variables.PlayerInteract.SELECTPERMISSION)) {
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}

				if (myZone.config.disabledWorlds.contains(location.getWorld().getName())) {
					player.sendMessage(tl("Error") + " " + tl("Creation_Select_Error1", location.getWorld().getName()));
					event.setCancelled(true);
					return;
				}
				selection.setPos1(location);
				setPos = 1;
				player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "1."));
				event.setCancelled(true);
			}
		}
		else if (action == Action.RIGHT_CLICK_BLOCK) {
			if (itemStack.getType().equals(myZone.config.createToolMaterial)) {
				if (!player.hasPermission(Variables.PlayerInteract.SELECTPERMISSION)) {
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}

				if (myZone.config.disabledWorlds.contains(location.getWorld().getName())) {
					player.sendMessage(tl("Error") + " " + tl("Creation_Select_Error1", location.getWorld().getName()));
					event.setCancelled(true);
					return;
				}
				selection.setPos2(location);
				setPos = 2;
				player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", "2."));
				event.setCancelled(true);
			}
			else if (itemStack.getType().equals(myZone.config.checkToolMaterial)) {
				if (!player.hasPermission(Variables.PlayerInteract.CHECKPERMISSION)) {
					player.sendMessage(tl("Error") + " " + tl("NoPermission"));
					event.setCancelled(true);
					return;
				}
				if (spamBlock.containsKey(player.getUniqueId())) {
					if (spamBlock.get(player.getUniqueId()) > System.currentTimeMillis()) {
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_SpamBlock", ((spamBlock.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000) + " " + tl("Text_Second", true)));
						event.setCancelled(true);
						return;
					}
				}

				ApplicableRegionSet set = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location);

				if (set.getRegions().size() == 0) {
					player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
					spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (myZone.config.delay.checkTool * 1000));
					event.setCancelled(true);
					return;
				}

				set.getRegions().forEach(region -> {
					if (region.getId().equalsIgnoreCase("__global__")) {
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
						spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (myZone.config.delay.checkTool * 1000));
						event.setCancelled(true);
						return;
					}

					if (region == null) {
						player.sendMessage(tl("Error") + " " + tl("Info_Tool_CheckFail"));
						spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (myZone.config.delay.checkTool * 1000));
						event.setCancelled(true);
						return;
					}

					player.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.GOLD + myZone.zoneManager.getRegionName(region.getId()));
					player.sendMessage(ChatColor.GRAY + tl("Text_Owners", true) + ": ");
					if (region.getOwners().getUniqueIds().size() == 0) {
						player.sendMessage(ChatColor.GOLD + "- " + "None");
					}
					else {
						for (UUID s : region.getOwners().getUniqueIds()) {
							player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
						}
					}
					player.sendMessage(ChatColor.GRAY + tl("Text_Location", true) + ": " + ChatColor.GOLD + location.getWorld().getName() + " - " + (region.getMinimumPoint() + " -> " + region.getMaximumPoint()));
					player.sendMessage(ChatColor.GRAY + tl("Text_Members", true) + ": ");
					if (region.getMembers().getUniqueIds().size() == 0) {
						player.sendMessage(ChatColor.GOLD + "- " + "None");
					}
					else {
						for (UUID s : region.getMembers().getUniqueIds()) {
							player.sendMessage(ChatColor.GOLD + "- " + Bukkit.getOfflinePlayer(s).getName());
						}
					}

					String temp = "";
					for (Flag flag : region.getFlags().keySet()) {
						temp += flag.getName() + ", ";
					}
					if (!Objects.equals(temp, "")) {
						temp = temp.substring(0, temp.length() - 2);
					}
					player.sendMessage(ChatColor.GRAY + tl("Text_Flags", true) + ": " + ChatColor.GOLD + (temp));
					player.sendMessage(ChatColor.GRAY + tl("Text_Type", true) + ": " + ChatColor.GOLD + (region.getType().getName()));
					spamBlock.put(player.getUniqueId(), System.currentTimeMillis() + (myZone.config.delay.checkTool * 1000));
					event.setCancelled(true);
				});
			}
		}

		if ((setPos != -1 && myZone.config.autoexpand.enabled) && (selection.getPos1() != null && selection.getPos2() != null)) {
			if (selection.getPos1().getY() == selection.getPos2().getY()) {
				int maxHeight = (int) ZoneUtils.getMaxSize(player).getY();

				if (maxHeight == -1) {
					maxHeight = player.getWorld().getMaxHeight();
				}

				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1.0F, 1.0F);

				InventoryMenuBuilder imb = new InventoryMenuBuilder(27).withTitle(tl("GUI_AutoExpand"));
				imb.withItem(11, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(tl("GUI_Cancel")).build());
				imb.withItem(15, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(tl("GUI_Confirm")).build());
				imb.show(player);

				int finalMaxHeight = maxHeight;
				int finalSet       = setPos;
				imb.onInteract((_player, _action, _event) -> {
					if (_event.getCurrentItem().getDurability() == (short) 13) {
						autoExpandZone(_player, location, finalSet, finalMaxHeight);
						_player.sendMessage(tl("Success") + " " + tl("Creation_Select_Border", finalSet));
					}
					_player.closeInventory();
				}, ClickType.LEFT);
			}
		}
	}

	private void autoExpandZone(Player player, Location defaultLocation, int finalSet, int maxHeight)
	{
		Selection selection = Selection.getSelection(player);
		if (finalSet == 1) {
			selection.setPos1(new Location(defaultLocation.getWorld(), defaultLocation.getBlockX(), maxHeight, defaultLocation.getBlockZ()));
		}
		else {
			selection.setPos2(new Location(defaultLocation.getWorld(), defaultLocation.getBlockX(), maxHeight, defaultLocation.getBlockZ()));
		}
	}
}
