package dev.jsinco.brewery.bukkit.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.event.Cancellable;

public interface Permissible extends Cancellable {

    boolean isDenied();

    Component getDenyMessage();
}
