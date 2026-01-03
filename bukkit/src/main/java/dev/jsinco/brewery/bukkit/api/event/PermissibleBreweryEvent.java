package dev.jsinco.brewery.bukkit.api.event;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;


public abstract class PermissibleBreweryEvent extends Event implements Cancellable {

    @Getter
    private dev.jsinco.brewery.api.util.CancelState cancelState;

    public PermissibleBreweryEvent(@NotNull dev.jsinco.brewery.api.util.CancelState cancelState) {
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

    public void setCancelState(@NotNull dev.jsinco.brewery.api.util.CancelState state) {
        this.cancelState = Preconditions.checkNotNull(state);
    }


}
