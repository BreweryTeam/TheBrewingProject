package dev.jsinco.brewery.event;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.util.BreweryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CustomEventRegistry {

    Map<BreweryKey, CustomEvent> customEvents = new HashMap<>();

    public static CustomEventRegistry.Builder builder() {
        return new Builder();
    }

    public void registerCustomEvent(CustomEvent customEvent) {
        Preconditions.checkNotNull(customEvent);
        customEvents.put(customEvent.key(), customEvent);
    }

    public @Nullable CustomEvent getCustomEvent(BreweryKey key) {
        Preconditions.checkNotNull(key);
        return customEvents.get(key);
    }

    public Collection<CustomEvent> events() {
        return customEvents.values();
    }

    public void clear() {
        customEvents.clear();
    }

    public static class Builder {


        List<CustomEvent> customEvents = new ArrayList<>();

        public Builder addEvent(@NotNull CustomEvent event) {
            Preconditions.checkNotNull(event);
            customEvents.add(event);
            return this;
        }

        public CustomEventRegistry build() {
            CustomEventRegistry output = new CustomEventRegistry();
            customEvents.forEach(output::registerCustomEvent);
            return output;
        }

    }
}
