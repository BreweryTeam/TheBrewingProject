package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryInsertEvent extends PermissibleBreweryEvent implements BrewModifiableEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final BukkitDistillery distillery;
    @Getter
    private final @Nullable Player player;
    @Getter
    @Setter
    private Brew brew;
    @Getter
    private final ItemStack itemRepresentation;


    public DistilleryInsertEvent(BukkitDistillery distillery, Brew brew, ItemStack itemStack, @Nullable Player player) {
        this.distillery = distillery;
        this.brew = brew;
        this.itemRepresentation = itemStack;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerLists() {
        return HANDLERS;
    }
}
