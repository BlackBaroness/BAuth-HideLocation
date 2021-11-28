package ru.baronessdev.free.auth.addons.hidelocation.teleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandTeleportationMethod extends TeleportationMethod {

    private final String command;

    public CommandTeleportationMethod(String command) {
        this.command = command;
    }

    @Override
    public boolean teleportToSpawn(Player p) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", p.getName()));
    }
}
