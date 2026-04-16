package dev.jsinco.brewery.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class TBPReloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
