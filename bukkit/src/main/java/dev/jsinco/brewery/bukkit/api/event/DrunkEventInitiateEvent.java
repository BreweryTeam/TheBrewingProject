package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.event.DrunkEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrunkEventInitiateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final DrunkEvent drunkenEvent;
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    private final @Nullable Player player;

    public DrunkEventInitiateEvent(DrunkEvent event, Player player) {
        this.drunkenEvent = event;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
