package dev.jsinco.brewery.api.event;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import net.kyori.adventure.text.Component;

import java.util.Locale;

/**
 * Preset drunken event
 */
public class NamedDrunkEvent implements DrunkEvent, BreweryKeyed, EventStepProperty {


    private final String name;
    @SerializedName("probability")
    private final EventProbability eventProbability;

    @Expose(serialize = false, deserialize = false)
    private BreweryKey key;


    public NamedDrunkEvent(String name, EventProbability eventProbability) {
        this.name = name;
        this.eventProbability = eventProbability;

        this.key = BreweryKey.parse(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public BreweryKey key() {
        if (this.key == null) {
            // TODO: I don't like this lazy init, GSON is forcing me to do it rn but I want to fix it later
            this.key = BreweryKey.parse(this.name.toLowerCase(Locale.ROOT));
        }
        return this.key;
    }

    @Override
    public Component displayName() {
        return Component.translatable("tbp.events.types." + name.toLowerCase(Locale.ROOT));
    }

    @Override
    public EventProbability probability() {
        return eventProbability;
    }

    public static NamedDrunkEvent fromKey(String key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return BreweryRegistry.DRUNK_EVENT.get(BreweryKey.parse(key));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NamedDrunkEvent namedOther)) {
            return false;
        }
        return this.key.equals(namedOther.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
}
