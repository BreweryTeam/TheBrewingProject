package dev.jsinco.brewery.bukkit.api.event;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Setter
public abstract class PermissibleBreweryEvent extends Event implements Cancellable {

    private boolean cancelled;
    @Getter
    private boolean denied;
    @Getter
    private @Nullable Component denyMessage;

    public boolean isCancelled() {
        return cancelled || denied;
    }

}
