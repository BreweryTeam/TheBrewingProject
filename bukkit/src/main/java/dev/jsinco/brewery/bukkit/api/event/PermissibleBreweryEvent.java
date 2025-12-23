package dev.jsinco.brewery.bukkit.api.event;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Setter
public abstract class PermissibleBreweryEvent extends Event implements Cancellable {

    private boolean cancelled = false;
    @Getter
    private boolean denied = false;
    @Getter
    private @Nullable Component denyMessage;

    public PermissibleBreweryEvent(boolean cancelled, boolean denied, @Nullable Component denyMessage) {
        this.cancelled = cancelled;
        this.denied = denied;
        this.denyMessage = denyMessage;
    }

    public PermissibleBreweryEvent(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public PermissibleBreweryEvent() {
        // NO-OP
    }

    public boolean isCancelled() {
        return cancelled || denied;
    }

}
