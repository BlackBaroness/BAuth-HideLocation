package ru.baronessdev.free.bauth_addons.hidelocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerLoginEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerPreLoginEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerPreRegisterEvent;

import java.util.HashSet;

public class Handler implements Listener {

    private final HideLocation plugin;
    private final Location spawn;

    private final HashSet<Player> teleportBypass = new HashSet<>();

    public Handler(HideLocation plugin, Location spawn) {
        this.plugin = plugin;
        this.spawn = spawn;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAuthPlayerPreLogin(AuthPlayerPreLoginEvent e) {
        needAuth(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAuthPlayerPreRegister(AuthPlayerPreRegisterEvent e) {
        needAuth(e.getPlayer());
    }

    private void needAuth(Player p) {
        if (!plugin.getData().contains(p.getName().toLowerCase())) return;

        teleportBypass.add(p);
        if (spawn == null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("command").replace("{player}", p.getName()));
            return;
        }

        p.teleport(spawn);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (teleportBypass.contains(e.getPlayer())) e.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onLogin(AuthPlayerLoginEvent e) {
        back(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        plugin.savePlayer(p);
        removeBypass(p);
    }

    public void back(Player p) {
        if (!plugin.getData().contains(p.getName().toLowerCase())) return; // no data - skip
        Location location = (Location) plugin.getData().get(p.getName().toLowerCase());
        p.teleport(location);
        removeBypass(p);
    }

    private void removeBypass(Player p) {
        teleportBypass.remove(p);
    }
}
