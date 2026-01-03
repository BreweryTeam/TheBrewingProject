package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.event.DrunkEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DrunkEventInitiateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final DrunkEvent drunkenEvent;
    @Getter
    @Setter
    private boolean cancelled;

    public DrunkEventInitiateEvent(DrunkEvent event) {
        this.drunkenEvent = event;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
