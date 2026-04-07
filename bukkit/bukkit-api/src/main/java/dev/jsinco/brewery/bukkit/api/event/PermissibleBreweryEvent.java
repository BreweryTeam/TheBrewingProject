package dev.jsinco.brewery.bukkit.api.event;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.util.CancelState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jspecify.annotations.NonNull;

public abstract class PermissibleBreweryEvent extends Event implements Cancellable {

    private dev.jsinco.brewery.api.util.CancelState cancelState;

    public PermissibleBreweryEvent(dev.jsinco.brewery.api.util.@NonNull CancelState cancelState) {
        this.cancelState = Preconditions.checkNotNull(cancelState);
    }

    public PermissibleBreweryEvent(boolean cancelled) {
        this.cancelState = cancelled ? new dev.jsinco.brewery.api.util.CancelState.Cancelled() : new dev.jsinco.brewery.api.util.CancelState.Allowed();
    }

    public PermissibleBreweryEvent() {
        // NO-OP
    }

    public boolean isCancelled() {
        return !(cancelState instanceof dev.jsinco.brewery.api.util.CancelState.Allowed);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelState = cancelled ? new dev.jsinco.brewery.api.util.CancelState.Cancelled() : new dev.jsinco.brewery.api.util.CancelState.Allowed();
    }

    public void setCancelState(dev.jsinco.brewery.api.util.@NonNull CancelState state) {
        this.cancelState = Preconditions.checkNotNull(state);
    }

    public CancelState getCancelState() {
        return this.cancelState;
    }
}
