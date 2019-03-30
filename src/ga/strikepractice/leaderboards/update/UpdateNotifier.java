package ga.strikepractice.leaderboards.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateNotifier implements Listener {

    private boolean updateAvailable;

    private final JavaPlugin plugin;
    private final String apiURL;
    private final String updatesURL;

    public UpdateNotifier(JavaPlugin plugin, int resourceId, boolean notifyStaff) {
        this.plugin = plugin;
        apiURL = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId + "/";
        updatesURL = "https://www.spigotmc.org/resources/" + resourceId + "/updates";
        if (notifyStaff) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
        checkUpdatesAsync();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && updateAvailable) {
            e.getPlayer().sendMessage(ChatColor.GRAY + "There is a new update available for " + ChatColor.AQUA
                    + plugin.getDescription().getName() + ChatColor.GRAY + ".");
            e.getPlayer().sendMessage(ChatColor.GRAY + "Link: " + updatesURL);
        }
    }

    public void checkUpdatesAsync() {
        new BukkitRunnable() {

            @Override
            public void run() {
                checkUpdates();
            }
        }.runTaskAsynchronously(plugin);
    }

    public boolean checkUpdates() {
        try {
            URLConnection conn = new URL(apiURL).openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String str1;
            while ((str1 = reader.readLine()) != null) {
                builder.append(str1);
            }
            String str2 = builder.toString();
            if (!plugin.getDescription().getVersion().equals(str2)) {
                updateAvailable = true;
                Bukkit.getLogger().info("There is a new update available for " + plugin.getDescription().getName());
                Bukkit.getLogger().info("Link: " + updatesURL);
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to check updates for " + plugin.getDescription().getName() + " "
                    + plugin.getDescription().getVersion());
        }
        return updateAvailable;
    }

    public boolean wasUpdateAvailable() {
        return updateAvailable;
    }
}
