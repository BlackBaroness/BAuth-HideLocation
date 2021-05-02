package ru.baronessdev.free.hidelocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;

import java.io.File;

public final class HideLocation extends JavaPlugin implements Listener {

    public YamlConfiguration data;
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

    public void back(Player p) {
        p.teleport((Location) data.get(p.getName().toLowerCase()));
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

    interface Expression {
        void execute();
    }
}
