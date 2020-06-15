package cx.wasabi.ish;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SerialPlayerState {
	
	private double health;
	private int food;
	private int xplevel;
	private float xpPoints;
	private ItemStack[] inventory;
	private int heldSlot;
	private ItemStack[] enderChest;
	
	public SerialPlayerState (double health, int food, int xplevel, float xpPoints, ItemStack[] inventory, int heldSlot, ItemStack[] enderChest) {
		this.health = health;
		this.food = food;
		this.xplevel = xplevel;
		this.xpPoints = xpPoints;
		this.inventory = inventory;
		this.heldSlot = heldSlot;
		this.enderChest = enderChest;
	}
	
	public void toPlayer(Player p) {
		p.setHealth(this.health);
		p.setFoodLevel(this.food);
		p.setLevel(this.xplevel);
		p.setExp(this.xpPoints);
		p.getInventory().setContents(this.inventory);
		p.getInventory().setHeldItemSlot(this.heldSlot);
		p.getEnderChest().setContents(this.enderChest);
	}
	
	public ConfigurationSection toConfig(ConfigurationSection cfg) {
		cfg.set("health", this.health);
		cfg.set("food", this.food);
		cfg.set("xp-level", this.xplevel);
		cfg.set("xp-remainder", this.xpPoints);
		cfg.set("inventoryHandSlot", heldSlot);
		List<String> itemStrings = new ArrayList<String>();
		for (int i=0; i < this.inventory.length; i++) {
			ItemStack thisStack = this.inventory[i];
			if (thisStack == null) {
				itemStrings.add(null);
				continue;
			}
			YamlConfiguration config = new YamlConfiguration();
	        config.set("i", thisStack);
			itemStrings.add(config.saveToString());
		}
		cfg.set("inv-backpack", itemStrings);
		//
		List<String> itemStringsB = new ArrayList<String>();
		for (int i=0; i < this.enderChest.length; i++) {
			ItemStack thisStack = this.enderChest[i];
			if (thisStack == null) {
				itemStringsB.add(null);
				continue;
			}
			YamlConfiguration config = new YamlConfiguration();
	        config.set("i", thisStack);
			itemStringsB.add(config.saveToString());
		}
		cfg.set("inv-ender", itemStrings);
		return cfg;
	}
	
	public static SerialPlayerState fromConfig(ConfigurationSection cfg) {
		List<?> back = cfg.getList("inv-backpack");
		ItemStack[] invBack = new ItemStack[back.size()];
		for (int i=0; i < back.size(); i++) {
			Object item = back.get(i);
			if (item instanceof String) {
				String itemString = (String) item;
				YamlConfiguration config = new YamlConfiguration();
		        try {
		            config.loadFromString(itemString);
		        } catch (Exception e) {
		            continue;
		        }
				invBack[i] = config.getItemStack("i", null);
			}
		}
		
		List<?> end = cfg.getList("inv-ender");
		ItemStack[] invEnd = new ItemStack[back.size()];
		for (int i=0; i < end.size(); i++) {
			Object item = end.get(i);
			if (item instanceof String) {
				String itemString = (String) item;
				YamlConfiguration config = new YamlConfiguration();
		        try {
		            config.loadFromString(itemString);
		        } catch (Exception e) {
		            continue;
		        }
				invEnd[i] = config.getItemStack("i", null);
			}
		}
		
		return new SerialPlayerState(cfg.getDouble("health"), cfg.getInt("food"), cfg.getInt("xp-level"), (float) cfg.getDouble("xp-remainder"), invBack, cfg.getInt("inventoryHandSlot"), invEnd);
	}
	
	public static SerialPlayerState fromPlayer(Player p) {
		double ph = p.getHealth();
		int pf = p.getFoodLevel();
		int pl = p.getLevel();
		float plf = p.getExp();
		
		PlayerInventory piv = p.getInventory();
		int hslot = piv.getHeldItemSlot();
		
		Inventory enderInv = p.getEnderChest();
		ItemStack[] invContents = piv.getContents();
		ItemStack[] enderContents = enderInv.getContents();
		
		return new SerialPlayerState(ph, pf, pl, plf, invContents, hslot, enderContents);
	}
	
}