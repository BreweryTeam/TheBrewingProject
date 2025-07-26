package dev.jsinco.brewery.event;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.util.BreweryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEventRegistry {

    Map<BreweryKey, CustomEvent.Keyed> customEvents = new HashMap<>();

    public static CustomEventRegistry.Builder builder() {
        return new Builder();
    }

    public void registerCustomEvent(CustomEvent.Keyed customEvent) {
        Preconditions.checkNotNull(customEvent);
        customEvents.put(customEvent.key(), customEvent);
    }

    public @Nullable CustomEvent.Keyed getCustomEvent(BreweryKey key) {
        Preconditions.checkNotNull(key);
        return customEvents.get(key);
    }

    public Collection<CustomEvent.Keyed> events() {
        return customEvents.values();
    }

    public void clear() {
        customEvents.clear();
    }

    public static class Builder {


        List<CustomEvent.Keyed> customEvents = new ArrayList<>();

        public Builder addEvent(@NotNull CustomEvent.Keyed event) {
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
