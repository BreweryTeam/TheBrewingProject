package dev.jsinco.brewery.bukkit.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public interface ItemModifiableEvent extends Cancellable {

    ItemStack getItemRepresentation();

    void setItemRepresentation(ItemStack item);
}
