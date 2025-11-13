package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BarrelExtractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitBarrel barrel;
    @Getter
    private final Brew brew;
    @Getter
    @Setter
    private ItemStack itemRepresentation;


    public BarrelExtractEvent(BukkitBarrel barrel, Brew brew, ItemStack itemRepresentation) {
        this.barrel = barrel;
        this.brew = brew;
        this.itemRepresentation = itemRepresentation;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
