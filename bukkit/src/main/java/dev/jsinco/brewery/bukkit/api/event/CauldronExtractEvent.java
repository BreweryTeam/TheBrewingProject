package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CauldronExtractEvent extends Event {

    @Getter
    private final BukkitCauldron cauldron;
    @Getter
    private final Brew brew;
    @Getter
    @Setter
    private ItemStack itemRepresentation;

    public CauldronExtractEvent(BukkitCauldron cauldron, Brew brew, ItemStack itemStack) {
        this.cauldron = cauldron;
        this.brew = brew;
        this.itemRepresentation = itemStack;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
