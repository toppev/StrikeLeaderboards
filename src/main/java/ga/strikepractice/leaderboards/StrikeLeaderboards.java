package ga.strikepractice.leaderboards;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.battlekit.BattleKit;
import ga.strikepractice.leaderboards.update.UpdateNotifier;
import ga.strikepractice.stats.DefaultPlayerStats;
import ga.strikepractice.stats.Stats;
import org.apache.commons.lang.Validate;
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

import java.util.*;
import java.util.Map.Entry;

public class StrikeLeaderboards extends JavaPlugin implements Listener, CommandExecutor {

    private final Set<SimpleIcon> statItems = new HashSet<>();

    private String title, format;
    private int leaderboardSize;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        title = ChatColor.translateAlternateColorCodes('&', getConfig().getString("title"));
        Validate.notNull(title, "'title' can not be null");
        format = ChatColor.translateAlternateColorCodes('&', getConfig().getString("format"));
        Validate.notNull(title, "'format' can not be null");
        leaderboardSize = getConfig().getInt("leaderboard-size");

        getConfig().getKeys(false).stream().filter(path -> getConfig().isConfigurationSection(path)).forEach(this::addItem);

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("strikeleaderboards").setExecutor(this);

        new UpdateNotifier(this, 59356, getConfig().getBoolean("notify-updates"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            openGUI((Player) sender);
        } else {
            sender.sendMessage("Console can not execute that command.");
        }
        return true;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() != null && e.getView().getTitle() != null && e.getView().getTitle().equals(title)) {
            e.setCancelled(true);
        }
    }

    private void addItem(String name) {
        if (getConfig().getBoolean(name + ".display")) {
            ItemStack item = getConfig().getItemStack(name + ".item");
            Validate.notNull(item, name + ".item can not be null");
            Validate.isTrue(getConfig().isSet(name + ".slot"));
            int slot = getConfig().getInt(name + ".slot");
            Validate.isTrue(slot >= 0, name + ".slot is not equal or greater than 0");
            statItems.add(new SimpleIcon(item, name, slot));
        }
    }

    private void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, getInventorySize(), title);
        int slot = 0;
        for (SimpleIcon icon : statItems) {
            ItemStack item = icon.getItem();
            inv.setItem(icon.getSlot(), applyTopLore(item, icon.getTag()));
            slot++;
        }
        if (!statItems.isEmpty()) {
            slot = 18;
        }
        for (BattleKit kit : StrikePractice.getAPI().getKits()) {
            if (kit.isElo() && kit.getIcon() != null) {
                inv.setItem(slot, applyTopLore(kit.getIcon().clone(), Stats.elo(kit.getName())));
                slot++;
            }
        }
        p.openInventory(inv);
    }

    private ItemStack applyTopLore(ItemStack item, String top) {
        ItemMeta meta = item.getItemMeta();
        LinkedHashMap<String, Double> list = DefaultPlayerStats.getTop().getOrDefault(top, new LinkedHashMap<String, Double>());
        List<String> lore = new ArrayList<>();
        if (list != null) {
            int i = 1;
            for (Entry<String, Double> e : list.entrySet()) {
                if (i <= leaderboardSize) {
                    lore.add(format.replace("<place>", i + "").replace("<player>", e.getKey()).replace("<value>", e.getValue().intValue() + ""));
                }
                i++;
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private int getInventorySize() {
        int size = 0;
        for (BattleKit kit : StrikePractice.getAPI().getKits()) {
            if (kit.isElo() && kit.getIcon() != null) {
                size++;
            }
        }
        if (!statItems.isEmpty()) {
            size += 18;
        }
        if (size > 54) {
            return 54;
        }
        // If it's multiple of 9 return it, otherwise size + (9 - size % 9) will return the next multiple of 9
        return size % 9 == 0 ? size : size + (9 - size % 9);
    }

}
