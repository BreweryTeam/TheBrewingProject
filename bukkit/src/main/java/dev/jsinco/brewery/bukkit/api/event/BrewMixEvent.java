package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.breweries.CauldronType;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BrewMixEvent extends BrewProcessEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final Cauldron cauldron;
    @Getter
    private final CauldronType cauldronType;
    @Getter
    private final boolean heated;

    public BrewMixEvent(Cauldron cauldron, CauldronType cauldronType, boolean heated, Brew source, Brew result) {
        super(source, result);
        this.cauldron = cauldron;
        this.cauldronType = cauldronType;
        this.heated = heated;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
