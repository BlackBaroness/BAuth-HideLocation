package ru.baronessdev.free.auth.addons.hidelocation;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataManager {

    private final File dataFile;
    private final YamlConfiguration dataCache;
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    public DataManager(JavaPlugin plugin) {
        dataFile = new File(plugin.getDataFolder() + File.separator + "data.yml");
        dataCache = YamlConfiguration.loadConfiguration(dataFile);
    }

    public @Nullable Location getLastLocation(Player p) {
        return (Location) dataCache.get(getNick(p));
    }

    public void setLastLocation(Player p, Location location) {
        dataCache.set(getNick(p), location);
        scheduleSaveTask();
    }

    public void deleteLastLocation(Player p) {
        dataCache.set(getNick(p), null);
        scheduleSaveTask();
    }

    private void scheduleSaveTask() {
        saveExecutor.execute(this::saveData);
    }

    @SneakyThrows
    private void saveData() {
        dataCache.save(dataFile);
    }

    private String getNick(Player p) {
        return p.getName().toLowerCase();
    }
}
