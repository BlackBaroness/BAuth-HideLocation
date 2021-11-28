package ru.baronessdev.free.auth.addons.hidelocation.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class TeleportationMethod implements Listener {

    public abstract boolean teleportToSpawn(Player p);

    public boolean teleportBackwards(Player p, @Nullable Location oldLocation) {
        if (oldLocation == null) return false;
        return teleport(p, oldLocation);
    }

    protected boolean teleport(Player p, Location location) {
        addTeleportHandler(p);
        return p.teleport(location);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    // ================================================

    private final Set<Player> teleportAllowed = new HashSet<>();

    protected void addTeleportHandler(Player p) {
        teleportAllowed.add(p);
    }

    public void handleTeleportEvent(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (!teleportAllowed.contains(p)) return;
        teleportAllowed.remove(p);
        e.setCancelled(false);
    }
}
