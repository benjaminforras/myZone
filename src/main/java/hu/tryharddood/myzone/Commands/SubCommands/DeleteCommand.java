package hu.tryharddood.myzone.Commands.SubCommands;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.Commands.Subcommand;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuListener;
import hu.tryharddood.myzone.Properties;
import hu.tryharddood.myzone.Variables;
import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class DeleteCommand extends Subcommand {

	@Override
	public String getPermission() {
		return Variables.PlayerCommands.DELETEPERMISSION;
	}

	@Override
	public String getUsage() {
		return "/zone delete <zone>";
	}

	@Override
	public String getDescription() {
		return tl("DeleteZone_Command_Description", true);
	}

	@Override
	public int getArgs() {
		return 2;
	}

	@Override
	public boolean playerOnly() {
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player   = (Player) sender;
		String regionID = myZone.zoneManager.getRegionID(args[1]);
		if (regionID == null)
		{
			sender.sendMessage(tl("Error") + " " + tl("ZoneNotFound", args[1]));
			return;
		}

		ProtectedRegion region = myZone.worldGuardHelper.getRegion(regionID);

		LocalPlayer lcOwner = myZone.worldGuardReflection.getWorldGuardPlugin().wrapPlayer(player);
		if (region.getOwners() == null || !region.getOwners().contains(lcOwner) && !player.hasPermission(Variables.PlayerCommands.DELETEOTHERSPERMISSION))
		{
			sender.sendMessage(tl("Error") + " " + tl("DeleteZone_Error1"));
			return;
		}

		InventoryMenuBuilder imb = new InventoryMenuBuilder(27).withTitle("Are you sure?");
		imb.withItem(11, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 13).setTitle(ChatColor.GREEN + tl("GUI_Confirm", true)).build());
		imb.withItem(15, new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 14).setTitle(ChatColor.GREEN + tl("GUI_Cancel", true)).build());
		imb.show(player);
		imb.onInteract(new InventoryMenuListener() {
			@Override
			public void interact(Player player, ClickType action, InventoryClickEvent event) {
				if (event.getCurrentItem() == null) return;

				if (event.getCurrentItem().getDurability() == (short) 13)
				{
					if (Properties.getEconomyEnabled())
					{
						if (!myZone.vaultEcon.has(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneDeleteMoney()))
						{
							player.sendMessage(tl("Error") + " " + tl("Economy_NotEnoughMoney", Properties.getZoneDeleteMoney()));
							return;
						}
						myZone.vaultEcon.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), Properties.getZoneDeleteMoney());
					}

					player.sendMessage(tl("Success") + " " + tl("DeleteZone_Success", args[1]));
					myZone.worldGuardHelper.deleteRegion(region);
					myZone.worldGuardHelper.saveAll();
				}
				player.closeInventory();
			}
		}, ClickType.LEFT);
	}
}
