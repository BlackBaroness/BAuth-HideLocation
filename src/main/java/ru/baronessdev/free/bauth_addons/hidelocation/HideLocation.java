package ru.baronessdev.free.bauth_addons.hidelocation;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.free.bauth_addons.hidelocation.util.UpdateCheckerUtil;
import ru.baronessdev.free.bauth_addons.hidelocation.util.logging.LogType;
import ru.baronessdev.free.bauth_addons.hidelocation.util.logging.Logger;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        checkUpdates();
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

    public void save() {
        Expression e = () -> {
            synchronized (HideLocation.class) {
                try {
                    data.save(file);
                } catch (Exception ignored) {
                }
            }
        };

        // асинхронное сохранение файла, если сервер не выключается
        if (disabling) {
            e.execute();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, e::execute);
        }
    }

    public YamlConfiguration getData() {
        return data;
    }

    interface Expression {
        void execute();
    }

    private void checkUpdates() {
        new Thread(() -> {
            try {
                int i = UpdateCheckerUtil.check(this);
                if (i != -1) {
                    Logger.log(LogType.INFO, "New version found: v" + ChatColor.YELLOW + i + ChatColor.GRAY + " (Current: v" + getDescription().getVersion() + ")");
                    Logger.log(LogType.INFO, "Update now: " + ChatColor.AQUA + "market.baronessdev.ru/shop/licenses/");
                }
            } catch (UpdateCheckerUtil.UpdateCheckException e) {
                Logger.log(LogType.ERROR, "Could not check for updates: " + e.getRootCause());
                Logger.log(LogType.ERROR, "Please contact Baroness's Dev if this isn't your mistake.");
            }
        }).start();
    }
}
