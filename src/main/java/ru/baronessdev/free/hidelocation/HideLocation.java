package ru.baronessdev.free.hidelocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.paid.auth.api.AuthQueryManagerAPI;
import ru.baronessdev.paid.auth.api.AuthSessionManagerAPI;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.events.AuthPlayerLoginEvent;

import java.io.File;

public final class HideLocation extends JavaPlugin implements Listener {

    private AuthQueryManagerAPI queryManager;
    private AuthSessionManagerAPI sessionManager;
    private YamlConfiguration data;
    private File file;
    private Location spawn;
    private boolean disabling = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration cfg = getConfig();
        if (!cfg.getBoolean("useCommand")) spawn = new Location(
                Bukkit.getWorld(cfg.getString("world")),
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                cfg.getInt("yaw"),
                cfg.getInt("pitch"));

        file = new File(getDataFolder() + File.separator + "data.yml");
        data = YamlConfiguration.loadConfiguration(file);
        queryManager = BaronessAuthAPI.getQueryManager();
        sessionManager = BaronessAuthAPI.getSessionManager();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!data.contains(p.getName().toLowerCase())) return; // игроку некуда возвращаться - пропускаем
        if (sessionManager.hasSession(p)) {
            back(p);
            return;
        }

        if (getConfig().getBoolean("useCommand")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("command").replace("{player}", p.getName()));
            return;
        }

        p.teleport(spawn);
    }

    @EventHandler
    private void onLogin(AuthPlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (!data.contains(p.getName().toLowerCase())) return; // игроку некуда возвращаться - пропускаем

        back(p);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        savePlayer(p);
    }

    private void savePlayer(Player p) {
        if (queryManager.getQuery(p) != null) return;

        data.set(p.getName().toLowerCase(), p.getLocation());
        save();
    }

    @Override
    public void onDisable() {
        disabling = true;
        Bukkit.getOnlinePlayers().forEach(this::savePlayer);
    }

    private void back(Player p) {
        p.teleport((Location) data.get(p.getName().toLowerCase()));
    }

    private void save() {
        Expression e = () -> {
            synchronized (HideLocation.class) {
                try {
                    data.save(file);
                } catch (Exception ignored) {
                }
            }
        };


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
