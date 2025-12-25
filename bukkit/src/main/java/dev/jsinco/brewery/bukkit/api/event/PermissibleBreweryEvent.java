package dev.jsinco.brewery.bukkit.api.event;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;


public abstract class PermissibleBreweryEvent extends Event implements Cancellable {

    @Getter
    private CancelState cancelState;

    public PermissibleBreweryEvent(@NotNull CancelState cancelState) {
        this.cancelState = Preconditions.checkNotNull(cancelState);
    }

    public PermissibleBreweryEvent(boolean cancelled) {
        this.cancelState = cancelled ? new CancelState.Cancelled() : new CancelState.Allowed();
    }

    public PermissibleBreweryEvent() {
        // NO-OP
    }

    public boolean isCancelled() {
        return !(cancelState instanceof CancelState.Allowed);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelState = cancelled ? new CancelState.Cancelled() : new CancelState.Allowed();
    }

    public void setCancelState(@NotNull CancelState state) {
        this.cancelState = Preconditions.checkNotNull(state);
    }


}
