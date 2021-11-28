package ru.baronessdev.free.auth.addons.hidelocation;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.baronessdev.free.auth.addons.hidelocation.teleport.TeleportationMethod;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.events.AuthPlayerLoginEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerRegisterEvent;
import ru.baronessdev.paid.auth.api.events.AuthPlayerSessionSavedEvent;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class Handler implements Listener {

    private final TeleportationMethod teleportation;
    private final DataManager dataManager;
    private final Logger logger;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!BaronessAuthAPI.getQueryManager().hasQueries(p)) {
            /*
             Если игрок выходит, не имея заморозки BaronessAuth, его локация сохраняется.
             Просто телепортировать игроков со спавна на свои родные локации и избежать сохранения не получится,
             так как во время выхода игрока его не удаётся телепортировать.
             */
            dataManager.setLastLocation(p, p.getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        Location lastLocation = dataManager.getLastLocation(p);
        if (lastLocation == null) {
            /*
            Нет локации, с которой игрок вышел. Это может произойти в 2х случаях:
                1. Игрок последний раз играл до того, как плагин был установлен.
                2. Игрок зашёл на сервер впервые.
            Так как игрока некуда возвращать, во избежание проблем ничего не делаем.
             */
            return;
        }

        // Телепортируем игрока до того момента, как BaronessAuth успеет применить заморозку
        if (!teleportation.teleportToSpawn(p)) {
            logger.warning("Could not teleport player " + p.getName() + " to spawn using " + teleportation.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        /* BaronessAuth может успеть применить правила заморозки, которые не дадут игроку телепортироваться.
        Это позволяет принудительно телепортировать игрока. */
        teleportation.handleTeleportEvent(e);
    }

    @EventHandler
    public void onRegister(AuthPlayerRegisterEvent e) {
        sendBack(e.getPlayer());
    }

    @EventHandler
    public void onLogin(AuthPlayerLoginEvent e) {
        sendBack(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSessionSaved(AuthPlayerSessionSavedEvent e) {
        sendBack(e.getPlayer());
    }

    private void sendBack(Player p) {
        if (!teleportation.teleportBackwards(p, dataManager.getLastLocation(p))) {
            logger.warning("Could not teleport player " + p.getName() + " to old location using " + teleportation.getName());
        }
    }
}
