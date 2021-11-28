package ru.baronessdev.free.auth.addons.hidelocation;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.free.auth.addons.hidelocation.teleport.CommandTeleportationMethod;
import ru.baronessdev.free.auth.addons.hidelocation.teleport.LocationTeleportationMethod;
import ru.baronessdev.free.auth.addons.hidelocation.teleport.TeleportationMethod;
import ru.baronessdev.lib.cloud.BaronessCloud;
import ru.baronessdev.lib.cloud.downloader.BaronessCloudDownloader;
import ru.baronessdev.lib.cloud.update.UpdateHandlerFactory;

public final class HideLocation extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new Handler(setupTeleportationMethod(), new DataManager(this), getLogger()), this);

        new Metrics(this, 11439);
        BaronessCloudDownloader.call(() -> {
            BaronessCloud baronessCloud = BaronessCloud.getInstance().addPlugin(this);
            baronessCloud.addUpdateHandler(this, UpdateHandlerFactory.createDefault(this));
        });
    }

    private TeleportationMethod setupTeleportationMethod() {
        FileConfiguration cfg = getConfig();
        TeleportationMethod teleport;

        if (cfg.getBoolean("useCommand")) {
            teleport = new CommandTeleportationMethod(cfg.getString("command"));
        } else {
            teleport = new LocationTeleportationMethod(
                    cfg.getString("world"),
                    cfg.getDouble("x"),
                    cfg.getDouble("y"),
                    cfg.getDouble("z"),
                    cfg.getInt("yaw"),
                    cfg.getInt("pitch")
            );
        }

        return teleport;
    }
}
