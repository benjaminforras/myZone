package hu.tryharddood.myzone.Zones;

import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

import static com.sk89q.worldedit.bukkit.BukkitUtil.toVector;

/*****************************************************
 *              Created by TryHardDood on 2016. 12. 27..
 ****************************************************/
public class Selection {

	private static final HashMap<UUID, Selection>   _selections = new HashMap<>();
	private              com.sk89q.worldedit.Vector _pos1       = null;
	private              World                      _pos1_World  = null;
	private              com.sk89q.worldedit.Vector _pos2       = null;
	private              World                      _pos2_World  = null;
	private Player _player;

	private Selection(Player player, com.sk89q.worldedit.Vector pos1, com.sk89q.worldedit.Vector pos2) {
		_player = player;
		_pos1 = pos1;
		_pos2 = pos2;
	}

	public static Selection getSelection(Player player) {
		if (!_selections.containsKey(player.getUniqueId()))
		{
			Selection selection = new Selection(player, null, null);
			_selections.put(player.getUniqueId(), selection);

			return selection;
		}

		Selection selection = _selections.get(player.getUniqueId());
		selection.setPlayer(player);

		return selection;
	}

	public void setPos1(com.sk89q.worldedit.Vector pos1) {
		_pos1 = pos1;
	}

	public com.sk89q.worldedit.Vector getPos1() {
		return _pos1;
	}

	public void setPos1(Location pos1) {
		_pos1 = toVector(pos1);
		_pos1_World = pos1.getWorld();
	}

	public void setPos2(com.sk89q.worldedit.Vector pos2) {
		_pos2 = pos2;
	}

	public com.sk89q.worldedit.Vector getPos2() {
		return _pos2;
	}

	public void setPos2(Location pos2) {
		_pos2 = toVector(pos2);
		_pos2_World = pos2.getWorld();
	}

	public CuboidRegion getCuboidRegion() {
		return new CuboidRegion(_pos1, _pos2);
	}

	public Player getPlayer() {
		return _player;
	}

	public void setPlayer(Player player) {
		_player = player;
	}

	public World getPos1World() {
		return _pos1_World;
	}

	public void setPos1World(World pos1_World) {
		_pos1_World = pos1_World;
	}

	public World getPos2World() {
		return _pos2_World;
	}

	public void setPos2World(World pos2_World) {
		_pos2_World = pos2_World;
	}
}
