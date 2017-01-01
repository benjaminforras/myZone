package hu.tryharddood.myzone.Util;

import hu.tryharddood.myzone.myZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/*****************************************************
 *
 *              BountifulAPI
 *                      by ConnorLinfoot {@see https://github.com/ConnorLinfoot/BountifulAPI/}
 *
 *              BossBarAPI
 *                      by Norfolf {@see https://github.com/Norfolf/}
 *
 *
 ****************************************************/
public class BountifulAPI {
	private static boolean useOldMethods = false;
	public static String nmsver;

	private static Map<String, FakeDragon> dragonMap = new HashMap<>();

	/**
	 * set the status of the fake dragon of a player
	 *
	 * @player: the player you want to set the fake dragon of
	 * @text: the text displayed above the bar
	 * @percent: the percentage of the bar that is full
	 * @reset: reset the fake dragon of the player or not
	 */
	public static void setStatus(Player player, String text, int percent, boolean reset) throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		FakeDragon fd = null;

		if (dragonMap.containsKey(player.getName()) & !reset)
		{
			fd = dragonMap.get(player.getName());
		}
		// create a new dragon if needed or if reset equals true
		else
		{
			// spawn it 400 blocks above the player so you don't see it die at 0 health
			fd = new FakeDragon(text, player.getLocation().add(0, 400, 0), percent);
			Object mobPacket = fd.getSpawnPacket();
			sendPacket(player, mobPacket);
			dragonMap.put(player.getName(), fd);
		}

		// set the status of the dragon and send the package to the player
		fd.setName(text);
		fd.setHealth(percent);
		Object metaPacket     = fd.getMetaPacket(fd.getWatcher());
		Object teleportPacket = fd.getTeleportPacket(player.getLocation().add(0, 400, 0));
		sendPacket(player, metaPacket);
		sendPacket(player, teleportPacket);
	}

	/**
	 * kill the dragon of a player
	 *
	 * @player: the player you want to kill the fake dragon of
	 */
	public static void killDragon(Player player) {
		try
		{
			sendPacket(player, dragonMap.get(player.getName()).getDestroyPacket());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		dragonMap.remove(player.getName());
	}

	/**
	 * check if the fake dragon of a player is killed or not
	 *
	 * @player: the player you want to check the fake dragon of
	 * @returns: true if the fake dragon of the player exists, else false
	 */
	public static boolean isKilled(Player player) {
		if (dragonMap.containsKey(player.getName()))
		{
			return true;
		}
		return false;
	}

	@Deprecated
	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
		sendTitle(player, fadeIn, stay, fadeOut, message, null);
	}

	@Deprecated
	public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
		sendTitle(player, fadeIn, stay, fadeOut, null, message);
	}

	@Deprecated
	public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
	}

	@Deprecated
	public static Integer getPlayerProtocol(Player player) {
		/* Returns the 1.8 protocol version as this is the only protocol a player can possibly be on with Spigot 1.8 */
		return 47;
	}

	private static void sendPacket(Player player, Object packet) {
		try
		{
			Object handle           = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try
		{
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static Method getMethod(Class<?> cl, String method) {
		for (Method m : cl.getMethods())
		{
			if (m.getName().equals(method))
			{
				return m;
			}
		}
		return null;
	}

	private static Method getMethod(Class<?> cl, String method, Class<?>[] args) {
		for (Method m : cl.getMethods())
		{
			if (m.getName().equals(method) && classListsEqual(args, m.getParameterTypes()))
			{
				return m;
			}
		}
		return null;
	}

	private static Field getField(Class<?> cl, String field) {
		try
		{
			return cl.getDeclaredField(field);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static Object getHandle(World world) {
		Object nmsWntity      = null;
		Method worldGetHandle = getMethod(world.getClass(), "getHandle");

		try
		{
			nmsWntity = worldGetHandle.invoke(world);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return nmsWntity;
	}

	private static boolean classListsEqual(Class<?>[] list1, Class<?>[] list2) {
		if (list1.length != list2.length)
		{
			return false;
		}

		for (int i = 0; i < list1.length; i++)
		{
			if (list1[i] != list2[i])
			{
				return false;
			}
		}
		return true;
	}

	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
		try
		{
			Object      e;
			Object      chatTitle;
			Object      chatSubtitle;
			Constructor subtitleConstructor;
			Object      titlePacket;
			Object      subtitlePacket;

			if (title != null)
			{
				title = ChatColor.translateAlternateColorCodes('&', title);
				title = title.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				//titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle, fadeIn, stay, fadeOut});
				titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
				sendPacket(player, titlePacket);

				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
				chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent")});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
				//titlePacket = subtitleConstructor.newInstance(new Object[]{e, chatTitle});
				titlePacket = subtitleConstructor.newInstance(e, chatTitle);
				sendPacket(player, titlePacket);
			}

			if (subtitle != null)
			{
				subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
				subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
				// Times packets
				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get(null);
				chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + title + "\"}");
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);

				e = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
				chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[]{String.class}).invoke(null, "{\"text\":\"" + subtitle + "\"}");
				//subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(new Class[]{getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE});
				subtitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE);
				//subtitlePacket = subtitleConstructor.newInstance(new Object[]{e, chatSubtitle, fadeIn, stay, fadeOut});
				subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
				sendPacket(player, subtitlePacket);
			}
		} catch (Exception var11)
		{
			var11.printStackTrace();
		}
	}

	public static void clearTitle(Player player) {
		sendTitle(player, 0, 0, 0, "", "");
	}

	public static void sendTabTitle(Player player, String header, String footer) {
		if (header == null) header = "";
		header = ChatColor.translateAlternateColorCodes('&', header);

		if (footer == null) footer = "";
		footer = ChatColor.translateAlternateColorCodes('&', footer);

		header = header.replaceAll("%player%", player.getDisplayName());
		footer = footer.replaceAll("%player%", player.getDisplayName());

		try
		{
			Object         tabHeader        = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + header + "\"}");
			Object         tabFooter        = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + footer + "\"}");
			Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(getNMSClass("IChatBaseComponent"));
			Object         packet           = titleConstructor.newInstance(tabHeader);
			Field          field            = packet.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(packet, tabFooter);
			sendPacket(player, packet);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void sendActionBar(Player player, String message) {
		try
		{
			Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
			Object   p  = c1.cast(player);
			Object   ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + nmsver + ".Packet");
			if (useOldMethods)
			{
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				//Method   m3  = c2.getDeclaredMethod("a", new Class<?>[]{String.class});
				Method m3  = c2.getDeclaredMethod("a", String.class);
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				//ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{cbc, (byte) 2});
				ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(cbc, (byte) 2);
			}
			else
			{
				Class<?> c2 = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				//Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(new Object[]{message});
				//ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(new Object[]{o, (byte) 2});
				Object o = c2.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				ppoc = c4.getConstructor(new Class<?>[]{c3, byte.class}).newInstance(o, (byte) 2);
			}
			//Method m1 = c1.getDeclaredMethod("getHandle", new Class<?>[]{});
			Method m1 = c1.getDeclaredMethod("getHandle");
			Object h  = m1.invoke(p);
			Field  f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			//Method m5 = pc.getClass().getDeclaredMethod("sendPacket", new Class<?>[]{c5});
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void sendActionBar(final Player player, final String message, int duration) {
		sendActionBar(player, message);

		if (duration >= 0)
		{
			// Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, "");
				}
			}.runTaskLater(myZone.myZonePlugin, duration + 1);
		}

		// Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
		while (duration > 60)
		{
			duration -= 60;
			int sched = duration % 60;
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, message);
				}
			}.runTaskLater(myZone.myZonePlugin, (long) sched);
		}
	}

	public static void sendActionBarToAllPlayers(String message) {
		sendActionBarToAllPlayers(message, -1);
	}

	public static void sendActionBarToAllPlayers(String message, int duration) {
		for (Player p : Bukkit.getOnlinePlayers())
		{
			sendActionBar(p, message, duration);
		}
	}

	public void onEnable() {
		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.equalsIgnoreCase("v1_7_"))
		{ // Not sure if 1_7 works for the protocol hack?
			useOldMethods = true;
		}
	}

	private static class FakeDragon {

		private static final int MAX_HEALTH = 200;
		private int    id;
		private int    x;
		private int    y;
		private int    z;
		private float  health;
		private String name;
		private Object world;
		private Object dragon;

		public FakeDragon(String name, Location loc, int percent) {
			this.name = name;
			this.x = loc.getBlockX();
			this.y = loc.getBlockY();
			this.z = loc.getBlockZ();
			this.health = percent / 100F * MAX_HEALTH;
			this.world = getHandle(loc.getWorld());
		}

		public void setHealth(int percent) {
			this.health = percent / 100F * MAX_HEALTH;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Object getSpawnPacket() throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			// make it so we don't need to update this plugin every time a new bukkit version is used
			Class<?> Entity            = getNMSClass("Entity");
			Class<?> EntityLiving      = getNMSClass("EntityLiving");
			Class<?> EntityEnderDragon = getNMSClass("EntityEnderDragon");
			dragon = EntityEnderDragon.getConstructor(getNMSClass("World")).newInstance(world);

			// in Entity
			Method setLocation = getMethod(EntityEnderDragon, "setPosition", new Class<?>[]{double.class, double.class, double.class});
			setLocation.invoke(dragon, x, y, z);

			Method setInvisible = getMethod(EntityEnderDragon, "setInvisible", new Class<?>[]{boolean.class});
			setInvisible.invoke(dragon, false);

			// in EntityInsentient
			Method setCustomName = getMethod(EntityEnderDragon, "setCustomName", new Class<?>[]{String.class});
			setCustomName.invoke(dragon, name);

			// in LivingEntity
			Method setHealth = getMethod(EntityEnderDragon, "setHealth", new Class<?>[]{float.class});
			setHealth.invoke(dragon, health);

			// in Entity
			Field motX = getField(Entity, "motX");
			motX.set(dragon, 0); // x velocity, double

			// in Entity
			Field motY = getField(Entity, "motY");
			motY.set(dragon, 0); // y velocity, double

			// in Entity
			Field motZ = getField(Entity, "motZ");
			motZ.set(dragon, 0); // z velocity, double

			// in Entity
			Method getId = getMethod(EntityEnderDragon, "getId", new Class<?>[]{});
			this.id = (Integer) getId.invoke(dragon);

			// get the spawn living entity packet class
			Class<?> PacketPlayOutSpawnEntityLiving = getNMSClass("PacketPlayOutSpawnEntityLiving");
			// create an instance of this class using dragon entity
			Object packet = PacketPlayOutSpawnEntityLiving.getConstructor(new Class<?>[]{EntityLiving}).newInstance(dragon);
			return packet;
		}

		public Object getDestroyPacket() throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
			// get the destroy entity packet class
			Class<?> PacketPlayOutEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy");
			// create a new instance of the class
			Object packet = PacketPlayOutEntityDestroy.getConstructor(new Class<?>[]{int[].class}).newInstance(new int[]{id});
			return packet;
		}

		public Object getMetaPacket(Object watcher) throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Class<?> DataWatcher                 = getNMSClass("DataWatcher");
			Class<?> PacketPlayOutEntityMetadata = getNMSClass("PacketPlayOutEntityMetadata");

			Object packet = PacketPlayOutEntityMetadata.getConstructor(new Class<?>[]{int.class, DataWatcher, boolean.class})
					.newInstance(id, watcher, true);
			return packet;
		}

		public Object getTeleportPacket(Location loc) throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Class<?> PacketPlayOutEntityTeleport = getNMSClass("PacketPlayOutEntityTeleport");
			Object packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[]{
					int.class, int.class, int.class, int.class, byte.class, byte.class})
					.newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32,
							(byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360));
			return packet;
		}

		public Object getWatcher() throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Class<?> Entity      = getNMSClass("Entity");
			Class<?> DataWatcher = getNMSClass("DataWatcher");

			Object watcher = DataWatcher.getConstructor(new Class<?>[]{Entity}).newInstance(dragon);
			Method a       = getMethod(DataWatcher, "a", new Class<?>[]{int.class, Object.class});

			a.invoke(watcher, 0, (byte) 0x20); // visible, 0 = true, 0x20 = false
			a.invoke(watcher, 6, (Float) health); // health
			a.invoke(watcher, 10, name); // name
			a.invoke(watcher, 11, (Byte) (byte) 1); // show name, 1 = true, 0 = false
			return watcher;
		}
	}
}