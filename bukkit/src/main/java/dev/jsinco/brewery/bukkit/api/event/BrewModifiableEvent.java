package dev.jsinco.brewery.bukkit.api.event;

import dev.jsinco.brewery.api.brew.Brew;
import org.bukkit.event.Cancellable;

public interface BrewModifiableEvent extends Cancellable {


    Brew getBrew();

    void setBrew(Brew brew);
}
