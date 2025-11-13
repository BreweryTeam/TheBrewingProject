package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DistilleryExtractEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitDistillery distillery;
    @Getter
    private final Brew brew;
    @Getter
    @Setter
    private ItemStack itemRepresentation;


    public DistilleryExtractEvent(BukkitDistillery distillery, Brew brew, ItemStack itemStack) {
        this.distillery = distillery;
        this.brew = brew;
        this.itemRepresentation = itemStack;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
