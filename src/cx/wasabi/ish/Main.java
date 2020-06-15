package cx.wasabi.ish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	private void consoleMessageSP(String msg) {
		System.out.println(ChatColor.BOLD + "[" + ChatColor.AQUA.toString() + getName() + ChatColor.WHITE.toString() + ChatColor.BOLD + "] " + ChatColor.RESET + msg);
	}
	
	@Override
	public void onEnable() {
		consoleMessageSP("Enabled! (Version " + getDescription().getVersion() + ")");
		consoleMessageSP("Registering Events...");
		getServer().getPluginManager().registerEvents(this, this);
		consoleMessageSP("Done!");
		consoleMessageSP("Checking for new versions...");
		consoleMessageSP(ChatColor.DARK_RED + "Version CDN not present. Assuming latest version.");
		//
		refreshGroups();
		System.out.println(groups);
	}
	
	@Override
	public void onDisable() {
		consoleMessageSP("Disabled.");
	}
	
	private List<InventoryGroup> groups = new ArrayList<InventoryGroup>();
	public List<InventoryGroup> getGroups(){
		return groups;
	}
	
	private SerialPlayerState getConfigInventory(Player ply, String group) {
		FileConfiguration cfg = getConfig();
		ConfigurationSection sec = cfg.getConfigurationSection("players");
		ConfigurationSection secPl;
		if (sec.contains(ply.getUniqueId().toString()) == false) {
			secPl = sec.createSection(ply.getUniqueId().toString());
			saveConfig();
		} else {
			secPl = sec.getConfigurationSection(ply.getUniqueId().toString());
		}
		ConfigurationSection secGrp;
		if (secPl.contains(group) == false) {
			SerialPlayerState uState = SerialPlayerState.fromPlayer(ply);
			secGrp = secPl.createSection(group);
			secGrp = uState.toConfig(secGrp);
			saveConfig();
		} else {
			secGrp = secPl.getConfigurationSection(group);
		}
		return SerialPlayerState.fromConfig(secGrp);
	}
	
	private void setConfigInventory(Player ply, String group) {
		getConfigInventory(ply, group);
		SerialPlayerState state = SerialPlayerState.fromPlayer(ply);
		FileConfiguration cfg = getConfig();
		ConfigurationSection groupCfg = cfg.getConfigurationSection("players").getConfigurationSection(ply.getUniqueId().toString()).getConfigurationSection(group);
		state.toConfig(groupCfg);
		saveConfig();
	}
	
	private void playerGroupListen(Player ply, World fromWorld, World toWorld, GameMode fromGM, GameMode toGM) {
		InventoryGroup fromMode = InventoryGroup.FromWorldMode(fromWorld, fromGM, groups);
		InventoryGroup toMode = InventoryGroup.FromWorldMode(toWorld, toGM, groups);
		String fromGroupName = "none";
		String toGroupName = "none";
		if (fromMode == null && toMode == null) {
			return;
		} else {
			if (fromMode != null) {
				fromGroupName = fromMode.getName();
			}
			if (toMode != null) {
				toGroupName = toMode.getName();
			}
		}
		if (fromGroupName.equalsIgnoreCase(toGroupName) == false) {
			setConfigInventory(ply, fromGroupName);
			SerialPlayerState destiState = getConfigInventory(ply, toGroupName);
			destiState.toPlayer(ply);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerGroupListenGM(PlayerGameModeChangeEvent e) {
		playerGroupListen(e.getPlayer(), e.getPlayer().getWorld(), e.getPlayer().getWorld(), e.getPlayer().getGameMode(), e.getNewGameMode());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerGroupListenWD(PlayerChangedWorldEvent e) {
		playerGroupListen(e.getPlayer(), e.getFrom(), e.getPlayer().getWorld(), e.getPlayer().getGameMode(), e.getPlayer().getGameMode());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void playerLeaveSaveInv(PlayerQuitEvent e) {
		InventoryGroup ig = InventoryGroup.FromPlayer(e.getPlayer(), groups);
		setConfigInventory(e.getPlayer(), ig.getName());
	}
	
	private FileConfiguration fetchConfig() {
		FileConfiguration config = getConfig();
		if (!config.contains("groups")) {
			consoleMessageSP("Creating default config (groups not present)");
			ConfigurationSection grp = config.createSection("groups");
			ConfigurationSection dft = grp.createSection("default");
			dft.set("worlds", (List<String>) Arrays.asList(new String[] {"world"}));
			dft.set("gamemodes", (List<String>) Arrays.asList(new String[] {"survival"}));
			saveConfig();
			consoleMessageSP("Done!");
		}
		if (!config.contains("players")) {
			consoleMessageSP("Creating default config (players not present)");
			config.createSection("players");
			saveConfig();
			consoleMessageSP("Done!");
		}
		return config;
	}
	
	private void refreshGroups() {
		FileConfiguration config = fetchConfig();
		ConfigurationSection cgroups = config.getConfigurationSection("groups");
		Map<String,?> vals = cgroups.getValues(false);
		List<World> allWorlds = getServer().getWorlds();
		for (String key : vals.keySet()) {
			ConfigurationSection grpInfo = cgroups.getConfigurationSection(key);
			@SuppressWarnings("unchecked")
			List<String> stWorlds = (List<String>) grpInfo.getList("worlds");
			List<World> worlds = new ArrayList<World>();
			for (int i=0; i < stWorlds.size(); i++) {
				String tWorldSt = stWorlds.get(i);
				for (int z=0; z < allWorlds.size(); z++) {
					World tWorld = allWorlds.get(z);
					if (tWorld.getName().equalsIgnoreCase(tWorldSt)) {
						worlds.add(tWorld);
					}
				}
			}
			@SuppressWarnings("unchecked")
			List<String> stModes = (List<String>) grpInfo.getList("gamemodes");
			List<GameMode> modes = new ArrayList<GameMode>();
			for (int i=0; i < stModes.size(); i++) {
				String tModeSt = stModes.get(i).toLowerCase();
				switch (tModeSt) {
					case "survival":
						modes.add(GameMode.SURVIVAL);
						break;
					case "adventure":
						modes.add(GameMode.ADVENTURE);
						break;
					case "creative":
						modes.add(GameMode.CREATIVE);
						break;
					case "spectator":
						modes.add(GameMode.SPECTATOR);
						break;
					default:
						consoleMessageSP(ChatColor.DARK_RED + "Config error: Unknown mode \"" + tModeSt + "\". Ignoring.");
				}
			}
			groups.add(new InventoryGroup(key, worlds, modes));
		}
	}
	
}