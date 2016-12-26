package hu.tryharddood.myzone.Util.WorldGuard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import hu.tryharddood.myzone.myZone;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/*****************************************************
 *              Created by TryHardDood on 2016. 12. 25..
 ****************************************************/
public class WorldGuardReflection {

	private static WorldGuardPlugin _worldGuardPlugin;

	private static final Method fuzzyMatchFlagMethod;
	private static final Method getFlagsMethod;
	private static final Method parseInputMethod;

	static {
		Method m = null;
		try {
			m = DefaultFlag.class.getMethod("fuzzyMatchFlag", String.class);
		} catch (Exception ignored) {}

		if(m == null)
		{
			try
			{
				m = DefaultFlag.class.getMethod("fuzzyMatchFlag", FlagRegistry.class, String.class);
			} catch (Exception ignored) {}
		}
		fuzzyMatchFlagMethod = m;

		m = null;
		try {
			m = Flag.class.getMethod("parseInput", WorldGuardPlugin.class, CommandSender.class, String.class);
		} catch (Exception ignored) {}

		if(m == null)
		{
			try
			{
				m = Flag.class.getMethod("parseInput", FlagContext.class);
			} catch (Exception ignored) {}
		}
		parseInputMethod = m;

		m = null;
		try
		{
			m = DefaultFlag.class.getMethod("getFlags");
		} catch (Exception ignored) {}
		getFlagsMethod = m;
	}

	public WorldGuardReflection(WorldGuardPlugin worldGuardPlugin)
	{
		_worldGuardPlugin = worldGuardPlugin;
	}

	public Object parseInput(Flag flag, CommandSender sender, ProtectedRegion region, String value) throws InvalidFlagFormat
	{
		if(parseInputMethod == null)
		{
			System.out.println("parseInput method not found!");
			return null;
		}

		if(parseInputMethod.getParameterCount() == 1)
		{
			try
			{
				return parseInputMethod.invoke(flag, (FlagContext.create().setSender(sender).setInput(value).setObject("region", region).build()));
			} catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				return parseInputMethod.invoke(flag, _worldGuardPlugin, sender, value);
			} catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public Flag<?>[] getFlags()
	{
		if(getFlagsMethod == null)
		{
			List<Flag<?>> flags = new ArrayList<>();

			for (Flag<?> flag : _worldGuardPlugin.getFlagRegistry()) {
				flags.add(flag);
			}
			return flags.toArray(new Flag<?>[flags.size()]);
		}
		else
		{
			try
			{
				return (Flag<?>[]) getFlagsMethod.invoke(myZone.myZonePlugin);
			} catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public Flag<?> fuzzyMatchFlag(String id)
	{
		if(fuzzyMatchFlagMethod == null)
		{
			System.out.println("fuzzyMatchFlag method not found!");
			return null;
		}

		if(fuzzyMatchFlagMethod.getParameterCount() == 1)
		{
			try
			{
				return (Flag<?>) fuzzyMatchFlagMethod.invoke(myZone.myZonePlugin, id);
			} catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				return (Flag<?>) fuzzyMatchFlagMethod.invoke(myZone.myZonePlugin, _worldGuardPlugin.getFlagRegistry(), id);
			} catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public WorldGuardPlugin getWorldGuardPlugin() {
		return _worldGuardPlugin;
	}
}
