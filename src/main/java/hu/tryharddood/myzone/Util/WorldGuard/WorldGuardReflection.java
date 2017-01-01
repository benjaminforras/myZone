
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
