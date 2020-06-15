package cx.wasabi.ish;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class InventoryGroup {
	
	private String name;
	public String getName() { return this.name; }
	private List<World> worlds;
	protected List<World> getWorlds() { return this.worlds; }
	private List<GameMode> modes;
	protected List<GameMode> getModes() { return this.modes; }
	
	public InventoryGroup(String name, List<World> worlds, List<GameMode> modes) {
		this.name = name;
		this.worlds = worlds;
		this.modes = modes;
	}
	
	@Override
	public String toString() {
		return "InventoryGroup (" + this.name + ")";
	}
	
	public static InventoryGroup FromPlayer(Player ply, List<InventoryGroup> all) {
		World playerWorld = ply.getWorld();
		GameMode playerMode = ply.getGameMode();
		return FromWorldMode(playerWorld, playerMode, all);
	}
	
	public static InventoryGroup FromWorldMode(World playerWorld, GameMode playerMode, List<InventoryGroup> all) {
		InventoryGroup ret = null;
		for (InventoryGroup ig : all){
			boolean hasWorld = false;
			boolean hasMode = false;
			System.out.println(ig.getWorlds());
			for (World wld : ig.getWorlds()) {
				System.out.println("Comparing " + wld.getName() + " to " + playerWorld.getName());
				if (wld.getName().equalsIgnoreCase(playerWorld.getName())) {
					hasWorld = true;
				}
			}
			for (GameMode gmd : ig.getModes()) {
				System.out.println("Comparing " + gmd.name() + " to " + playerMode.name());
				if (gmd.equals(playerMode)) {
					hasMode = true;
				}
			}
			if (hasWorld && hasMode) ret = ig;
		}
		return ret;
	}
	
}