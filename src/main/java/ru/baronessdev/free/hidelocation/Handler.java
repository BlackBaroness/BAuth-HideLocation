package ru.baronessdev.free.hidelocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.baronessdev.paid.auth.api.AuthSessionManagerAPI;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.events.AuthPlayerLoginEvent;

public class Handler implements Listener {

    private final HideLocation plugin;
    private final Location spawn;
    private final AuthSessionManagerAPI sessionManager;

    public Handler(HideLocation plugin, Location spawn) {
        this.plugin = plugin;
        this.spawn = spawn;

        sessionManager = BaronessAuthAPI.getSessionManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!plugin.data.contains(p.getName().toLowerCase())) return; // игроку некуда возвращаться - пропускаем
        if (sessionManager.hasSession(p)) {
            plugin.back(p);
            return;
        }

        if (spawn == null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("command").replace("{player}", p.getName()));
            return;
        }

        p.teleport(spawn);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onLogin(AuthPlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (!plugin.data.contains(p.getName().toLowerCase())) return; // игроку некуда возвращаться - пропускаем

        plugin.back(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onQuit(PlayerQuitEvent event) {
        plugin.savePlayer(event.getPlayer());
    }
}
