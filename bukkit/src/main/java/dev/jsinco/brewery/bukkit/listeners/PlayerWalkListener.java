package dev.jsinco.brewery.bukkit.listeners;

import org.bukkit.Input;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerWalkListener implements Listener {


    private Map<UUID, Vector> movements = new HashMap<>();


    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Input input = event.getPlayer().getCurrentInput();
        if (input.isBackward() || input.isForward() || input.isLeft() || input.isRight()) {
            movements.put(event.getPlayer().getUniqueId(), event.getTo().toVector().subtract(event.getFrom().toVector()));
        } else {
            movements.put(event.getPlayer().getUniqueId(), new Vector());
        }
    }

    public @Nullable Vector getRegisteredMovement(UUID player) {
        return movements.get(player);
    }
}
