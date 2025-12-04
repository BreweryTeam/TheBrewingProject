package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CauldronInsertEvent extends PermissibleBreweryEvent implements BrewModifiableEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final BukkitCauldron cauldron;
    @Getter
    @Setter
    private Brew brew;
    @Getter
    private final ItemStack itemRepresentation;

    public CauldronInsertEvent(BukkitCauldron cauldron, Brew brew, ItemStack itemStack, @Nullable Player player) {
        this.cauldron = cauldron;
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
