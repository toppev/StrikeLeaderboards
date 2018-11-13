package ga.strikepractice.leaderboards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import ga.strikepractice.StrikePracticeAPI;
import ga.strikepractice.battlekit.BattleKit;
import ga.strikepractice.stats.PlayerStats;
import ga.strikepractice.stats.Stats;

public class StrikeLeaderboards extends JavaPlugin implements Listener, CommandExecutor {

	
	private String title, format;
	private int leaderboardSize;
	
	private final List<SimpleIcon> statItems = new ArrayList<SimpleIcon>();

	
	@Override
	public void onEnable() {
		saveDefaultConfig();
	
		title = ChatColor.translateAlternateColorCodes('&', getConfig().getString("title"));
		format = ChatColor.translateAlternateColorCodes('&', getConfig().getString("format"));
		leaderboardSize = getConfig().getInt("leaderboard-size");
		
		addItem("kills");
		addItem("deaths");
		addItem("global-elo");
		addItem("lms");
		addItem("brackets");
		addItem("party-vs-party-wins");
		
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("leaderboards").setExecutor(this);
	}

	private void addItem(String name) {
		if(getConfig().getBoolean(name + ".display")) {
			statItems.add(new SimpleIcon(getConfig().getItemStack(name + ".item"), name, getConfig().getInt(name + ".slot")));
		}
	}


	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getInventory() != null && e.getInventory().getTitle().equalsIgnoreCase(title)) {
			e.setCancelled(true);
		}
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			openGUI((Player) sender);
		}
		return true;
	}


	private void openGUI(Player p) {
		Inventory inv = Bukkit.createInventory(null, getSize(), title);
		try {
			int slot = 0;
			for(SimpleIcon icon : statItems) {
				ItemStack item = icon.getItem();
				inv.setItem(icon.getSlot(), applyTop(item, icon.getTag()));
				slot++;
			}
			if(!statItems.isEmpty()) slot = 18;
			for(BattleKit kit : StrikePracticeAPI.getStrikePractice().kits) {
				if(kit.isElo() && kit.getIcon() != null) {
					inv.setItem(slot, applyTop(kit.getIcon().clone(), Stats.elo(kit.getName())));
					slot++;
				}
			}
		}catch(Exception e) {}
		p.openInventory(inv);
	}

	private ItemStack applyTop(ItemStack item, String top) {
		ItemMeta meta = item.getItemMeta();
		LinkedHashMap<String, Double> list = PlayerStats.getTop().getOrDefault(top, new LinkedHashMap<String, Double>());
		List<String> lore = new ArrayList<String>();
		if(list != null) {
			int i = 1;
			for(Entry<String, Double> e : list.entrySet()) {
				if(i <= leaderboardSize) {
					lore.add(format.replace("<place>", i + "").replace("<player>", e.getKey()).replace("<value>", e.getValue().intValue() + ""));
				}
				i++;
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private int getSize() {
		int size = 0;
		for(BattleKit kit : StrikePracticeAPI.getStrikePractice().kits) {
			if(kit.isElo() && kit.getIcon() != null) {
				size++;
			}
		}
		if(!statItems.isEmpty()) size += 18;
		if (size <= 9) return 9;
		if (size <= 18) return 18;
		if (size <= 27) return 27;
		if (size <= 36) return 36;
		if (size <= 45) return 45;
		return 54;
	}
}
