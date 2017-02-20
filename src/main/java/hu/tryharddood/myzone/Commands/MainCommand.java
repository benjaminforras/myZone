package hu.tryharddood.myzone.Commands;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.MenuBuilder.ItemBuilder;
import hu.tryharddood.myzone.MenuBuilder.PageLayout;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuBuilder;
import hu.tryharddood.myzone.MenuBuilder.inventory.InventoryMenuListener;
import hu.tryharddood.myzone.MenuBuilder.inventory.PageInventoryBuilder;
import hu.tryharddood.myzone.Util.HeadTextureChanger;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static hu.tryharddood.myzone.Util.Localization.I18n.tl;


public class MainCommand extends Subcommand
{

	private static ArrayList<Flag> _flagsCache = new ArrayList<>();
	private ProtectedRegion region;

	private InventoryMenuListener flagSettingListener  = new InventoryMenuListener()
	{
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event)
		{
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			/*ItemStack item = _pageInventory.getInventory().getItem(slot);

			int newPage = 0;
			if (item.equals(_pageInventory.getBackPage())) {
				newPage = -1;
			}
			else if (item.equals(_pageInventory.getForwardsPage())) {
				newPage = 1;
			}
			if (newPage != 0) {
				_pageInventory.setPage(_pageInventory.getCurrentPage() + newPage);
				return;
			}*/
			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

			Flag flag = myZone.worldGuardReflection.fuzzyMatchFlag(ChatColor.stripColor(itemName));

			Object value;

			if (action == ClickType.RIGHT) {
				Bukkit.dispatchCommand(player, "zone flag " + myZone.zoneManager.getRegion(region.getId()).getZoneName() + " " + flag.getName());
				event.getInventory().setItem(event.getSlot(), new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(ChatColor.GRAY + tl("GUI_FlagNotSet")).build());
				player.updateInventory();
				return;
			}

			String prefix = "";
			if (flag instanceof StateFlag) {
				value = region.getFlag(flag) == StateFlag.State.ALLOW ? "deny" : "allow";

				if (value == "allow") {
					prefix = ChatColor.GREEN + tl("GUI_Allow");
				}
				else if (value == "deny") {
					prefix = ChatColor.RED + tl("GUI_Deny");
				}
				else {
					prefix = ChatColor.GRAY + tl("GUI_FlagNotSet");
				}
			}
			else if (flag instanceof BooleanFlag) {
				value = region.getFlag(flag) == (Object) true ? "false" : "true";

				if (value == "true") {
					prefix = ChatColor.GREEN + tl("GUI_Allow");
				}
				else if (value == "false") {
					prefix = ChatColor.RED + tl("GUI_Deny");
				}
				else {
					prefix = ChatColor.GRAY + tl("GUI_FlagNotSet");
				}
			}
			else {
				value = "Unknown";
			}

			Bukkit.dispatchCommand(player, "zone flag " + myZone.zoneManager.getRegionName(region.getId()) + " " + flag.getName() + " " + value.toString());

			event.getInventory().setItem(event.getSlot(), new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build());
			player.updateInventory();
		}
	};
	private InventoryMenuListener removeMemberListener = new InventoryMenuListener()
	{
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event)
		{
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			String itemName   = event.getCurrentItem().getItemMeta().getDisplayName();
			String playerName = ChatColor.stripColor(itemName);
			player.closeInventory();
			Bukkit.dispatchCommand(player, "zone members " + myZone.zoneManager.getRegionName(region.getId()) + " remove " + playerName);
		}
	};
	private InventoryMenuListener removeOwnerListener  = new InventoryMenuListener()
	{
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event)
		{
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			String itemName   = event.getCurrentItem().getItemMeta().getDisplayName();
			String playerName = ChatColor.stripColor(itemName);
			player.closeInventory();
			Bukkit.dispatchCommand(player, "zone owners " + myZone.zoneManager.getRegionName(region.getId()) + " remove " + playerName);
		}
	};

	private InventoryMenuListener settingsMenuListener = new InventoryMenuListener()
	{
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event)
		{
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			if (region == null) return;

			ItemStack            itemStack  = event.getCurrentItem();
			ArrayList<ItemStack> itemStacks = new ArrayList<>();
			if (itemStack.getType() == Material.SIGN) {
				itemStacks.clear();

				String prefix = null;
				for (Flag flag : _flagsCache) {
					if (!player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", flag.getName())) && !player.hasPermission(Variables.PlayerCommands.FLAGTYPEPERMISSION.replaceAll("\\[flag\\]", "*"))) {
						continue;
					}

					if (itemStacks.contains(new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build())) {
						continue;
					}

					if (flag instanceof StateFlag) {
						if (region.getFlag(flag) == StateFlag.State.ALLOW) {
							prefix = ChatColor.GREEN + tl("GUI_Allow");
						}
						else if (region.getFlag(flag) == StateFlag.State.DENY) {
							prefix = ChatColor.RED + tl("GUI_Deny");
						}
						else {
							prefix = ChatColor.GRAY + tl("GUI_FlagNotSet");
						}

					}
					else if (flag instanceof BooleanFlag) {
						if (region.getFlag(flag) == (Object) true) {
							prefix = ChatColor.GREEN + tl("GUI_Allow");
						}
						else if (region.getFlag(flag) == (Object) false) {
							prefix = ChatColor.RED + tl("GUI_Deny");
						}
						else {
							prefix = ChatColor.GRAY + tl("GUI_FlagNotSet");
						}
					}

					itemStacks.add(new ItemBuilder(Material.SIGN).setTitle(flag.getName()).addLore(prefix).build());
				}

				PageInventoryBuilder pageInventory = new PageInventoryBuilder(tl("GUI_Flags"), itemStacks);
				pageInventory.show(player);

				pageInventory.onInteract(flagSettingListener, ClickType.LEFT, ClickType.RIGHT);
			}
			else if (itemStack.getType() == Material.SKULL_ITEM) {
				if (itemStack.getItemMeta().getDisplayName().contains(tl("GUI_Members"))) {
					if (region.getMembers() == null) {
						return;
					}
					List<UUID> members = new ArrayList<>();
					members.addAll(region.getMembers().getUniqueIds());

					itemStacks.clear();
					ItemStack headItem  = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
					SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
					for (UUID member : members) {
						//skullMeta.setOwner(Bukkit.getOfflinePlayer(member).getName());
						try {
							HeadTextureChanger.applyTextureToMeta(skullMeta, HeadTextureChanger.createProfile(HeadTextureChanger.encodeBase64(/*String.format("https://crafatar.com/skins/%s", member)*/"https://crafatar.com/skins/7ac3c39f-23d5-472a-a7c9-24798265fa15".getBytes())));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						headItem.setItemMeta(skullMeta);

						itemStacks.add(new ItemBuilder(headItem).setTitle(ChatColor.GRAY + Bukkit.getOfflinePlayer(member).getName()).build());
					}

					PageInventoryBuilder pageInventory = new PageInventoryBuilder(tl("GUI_Members"), itemStacks);
					pageInventory.show(player);

					pageInventory.onInteract(removeMemberListener, ClickType.LEFT);
				}
				else {
					if (region.getOwners() == null) {
						return;
					}
					List<UUID> owners = new ArrayList<>();
					owners.addAll(region.getOwners().getUniqueIds());

					itemStacks.clear();
					ItemStack headItem  = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
					SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
					for (UUID owner : owners) {
						//skullMeta.setOwner(Bukkit.getOfflinePlayer(owner).getName());
						try {
							HeadTextureChanger.applyTextureToMeta(skullMeta, HeadTextureChanger.createProfile(HeadTextureChanger.encodeBase64(/*String.format("https://crafatar.com/skins/%s", owner)*/"https://crafatar.com/skins/7ac3c39f-23d5-472a-a7c9-24798265fa15".getBytes())));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						headItem.setItemMeta(skullMeta);

						itemStacks.add(new ItemBuilder(headItem).setTitle(ChatColor.GRAY + Bukkit.getOfflinePlayer(owner).getName()).build());
					}

					PageInventoryBuilder pageInventory = new PageInventoryBuilder(tl("GUI_Owners"), itemStacks);
					pageInventory.show(player);

					pageInventory.onInteract(removeOwnerListener, ClickType.LEFT);
				}
			}
			else if (itemStack.getType() == Material.BARRIER) {
				Bukkit.dispatchCommand(player, "zone delete " + myZone.zoneManager.getRegionName(region.getId()));
			}
		}
	};
	private InventoryMenuListener mainMenuListener     = new InventoryMenuListener()
	{
		@Override
		public void interact(Player player, ClickType action, InventoryClickEvent event)
		{
			if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
			if (myZone.zoneManager.getRegionID(ChatColor.stripColor(itemName)) != null) {
				if (myZone.worldGuardHelper.getPlayerMemberRegions(player.getUniqueId()).contains(ChatColor.stripColor(itemName))) {
					return;
				}
				region = myZone.worldGuardHelper.getRegion(myZone.zoneManager.getRegionID(ChatColor.stripColor(itemName)));

				InventoryMenuBuilder imb = new InventoryMenuBuilder(45).withTitle(ChatColor.stripColor(itemName));

				ArrayList<ItemStack> itemStacks = new ArrayList<>();
				itemStacks.add(new ItemBuilder(Material.SIGN).setTitle(ChatColor.GREEN + tl("GUI_Flags")).addLore(tl("GUI_FlagLore_Desc"), "", tl("GUI_FlagLore_Left"), tl("GUI_FlagLore_Right")).build());
				itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GREEN + tl("GUI_Members")).addLore(tl("GUI_MembersLore_Desc"), "", tl("GUI_MembersLore_Left"), tl("GUI_MembersLore_Right")).build());
				itemStacks.add(new ItemBuilder(Material.SKULL_ITEM, (short) 3).setTitle(ChatColor.GREEN + tl("GUI_Owners")).addLore(tl("GUI_OwnersLore_Desc"), "", tl("GUI_OwnersLore_Left"), tl("GUI_OwnersLore_Right")).build());
				itemStacks.add(new ItemBuilder(Material.BARRIER).setTitle(ChatColor.GREEN + tl("GUI_Delete")).addLore("", tl("GUI_DeleteLore_Desc")).build());

				PageLayout pageLayout = new PageLayout("XXXXOXXXX", "XXXXXXXXX", "XXOXXXOXX", "XXXXXXXXX", "XXXXOXXXX");
				imb.withItems(pageLayout.generate(itemStacks));

				imb.show(player);
				imb.onInteract(settingsMenuListener, ClickType.LEFT);
			}
		}
	};

	@Override
	public String getPermission()
	{
		return Variables.PlayerCommands.MAINPERMISSION;
	}

	@Override
	public String getUsage()
	{
		return "/zone";
	}

	@Override
	public String getDescription()
	{
		return tl("Main_Command_Description");
	}

	@Override
	public int getArgs()
	{
		return 0;
	}

	@Override
	public boolean playerOnly()
	{
		return true;
	}

	@Override
	public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (_flagsCache == null || _flagsCache.isEmpty()) {
			_flagsCache.addAll(myZone.config.flags.stream().filter(flag -> flag instanceof StateFlag || flag instanceof BooleanFlag).collect(Collectors.toList()));
		}

		Player player = (Player) sender;

		HashMap<String, List<String>> zones;
		if (!myZone.zoneManager.getCache().containsKey(player.getUniqueId())) {
			myZone.zoneManager.updateCacheForPlayer(player.getUniqueId());
		}
		zones = myZone.zoneManager.getCache().get(player.getUniqueId());

		List<String> owned  = zones.get("owned");
		List<String> member = zones.get("member");

		int zonesSize   = zones.size();
		int membersSize = member.size();
		int size        = zonesSize + membersSize;

		if (size == 0) {
			player.sendMessage(tl("Error") + " " + tl("no_zones"));
			return;
		}

		ArrayList<ItemStack> itemStacks = new ArrayList<>();
		for (String s : owned) {
			itemStacks.add(new ItemBuilder(Material.WOOD_DOOR).setTitle(ChatColor.YELLOW + s).build());
		}

		for (String s : member) {
			itemStacks.add(new ItemBuilder(Material.ACACIA_DOOR_ITEM).setTitle(ChatColor.YELLOW + s).build());
		}

		PageInventoryBuilder pageInventory = new PageInventoryBuilder("myZone GUI", itemStacks);
		pageInventory.show(player);

		pageInventory.onInteract(mainMenuListener, ClickType.LEFT);
	}

	/**
	 * https://mcapi.ca/rawskin/username
	 * https://crafatar.com/skins/uuid
	 *
	 * @param url
	 * @return
	 */

	/*public ItemStack getSkull(String url)
	{
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		if (url.isEmpty()) return head;

		Class  gameProfileClass    = new ClassResolver().resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile");
		Method getPropertiesMethod = new MethodResolver(gameProfileClass).resolveSilent("getProperties");

		SkullMeta   headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile  = new GameProfile(UUID.randomUUID(), null);
		//byte[]      encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
		byte[] encodedData = Base64Coder.encodeString("{textures:{SKIN:{url:\"" + url + "\"}}}").getBytes();
		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		}
		catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}*/
}
