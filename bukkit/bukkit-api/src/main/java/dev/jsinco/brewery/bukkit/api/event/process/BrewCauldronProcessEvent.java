package dev.jsinco.brewery.bukkit.api.event.process;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.Cauldron;
import dev.jsinco.brewery.api.breweries.CauldronType;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

/**
 * Will only trigger whenever an ingredient is added to the cauldron
 */
public class BrewCauldronProcessEvent extends BrewProcessEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Cauldron cauldron;
    private final CauldronType cauldronType;
    private final boolean heated;

    public BrewCauldronProcessEvent(Cauldron cauldron, CauldronType cauldronType, boolean heated, Brew source, Brew result) {
        super(source, result);
        this.cauldron = cauldron;
        this.cauldronType = cauldronType;
        this.heated = heated;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Cauldron getCauldron() {
        return this.cauldron;
    }

    public CauldronType getCauldronType() {
        return this.cauldronType;
    }

    public boolean isHeated() {
        return this.heated;
    }
}
