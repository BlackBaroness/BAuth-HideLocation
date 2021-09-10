package ru.baronessdev.free.bauth_addons.hidelocation;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.lib.cloud.BaronessCloud;
import ru.baronessdev.lib.cloud.downloader.BaronessCloudDownloader;
import ru.baronessdev.lib.cloud.update.UpdateHandlerFactory;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;

import java.io.File;

public final class HideLocation extends JavaPlugin implements Listener {

    private YamlConfiguration data;
    private File file;
    private boolean disabling;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        Location spawn = null;

        if (!cfg.getBoolean("useCommand")) spawn = new Location(
                Bukkit.getWorld(cfg.getString("world")),
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                cfg.getInt("yaw"),
                cfg.getInt("pitch"));

        file = new File(getDataFolder() + File.separator + "data.yml");
        data = YamlConfiguration.loadConfiguration(file);

        Bukkit.getPluginManager().registerEvents(new Handler(this, spawn), this);
        new Metrics(this, 11439);

        BaronessCloudDownloader.call(() -> {
            BaronessCloud baronessCloud = BaronessCloud.getInstance().addPlugin(this);
            baronessCloud.addUpdateHandler(this, UpdateHandlerFactory.createDefault(this));
        });
    }

    @Override
    public void onDisable() {
        disabling = true;
        Bukkit.getOnlinePlayers().forEach(this::savePlayer);
    }

    public void savePlayer(Player p) {
        if (BaronessAuthAPI.getQueryManager().getQuery(p) != null) return;

        data.set(p.getName().toLowerCase(), p.getLocation());
        save();
    }

    /*
    Asynchronous writing of data to a YAML file.
    The synchronization block is needed because saving in full asynchronous causes exceptions.
    But this method correctly calls the Bukkit Async Task.

    Why save it in runtime?
    Because we don't want problems for customers if they lose data.
     */

    public void save() {
        Runnable r = () -> {
            synchronized (HideLocation.class) {
                try {
                    data.save(file);
                } catch (Exception ignored) {
                }
            }
        };

        // registering Bukkit Task while disabling is impossible so we need check
        if (disabling) {
            r.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, r);
        }
    }

    public YamlConfiguration getData() {
        return data;
    }
}
