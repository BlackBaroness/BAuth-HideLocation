package ru.baronessdev.free.auth.addons.hidelocation.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class LocationTeleportationMethod extends TeleportationMethod {

    private final Location spawn;

    public LocationTeleportationMethod(String world, double x, double y, double z, int yaw, int pitch) {
        spawn = new Location(
                Bukkit.createWorld(new WorldCreator(world)),
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    @Override
    public boolean teleportToSpawn(Player p) {
        return teleport(p, spawn);
    }
}
