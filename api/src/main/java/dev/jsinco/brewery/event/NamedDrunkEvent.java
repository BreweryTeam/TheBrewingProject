package dev.jsinco.brewery.event;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryKeyed;
import dev.jsinco.brewery.util.Registry;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public class NamedDrunkEvent implements DrunkEvent, BreweryKeyed, EventStepProperty {


    private final String name;
    @SerializedName("alcohol_requirement")
    private final int alcoholRequirement;
    @SerializedName("toxins_requirement")
    private final int toxinsRequirement;
    @SerializedName("probability_weight")
    private final int probabilityWeight;

    @Expose(serialize = false, deserialize = false)
    private BreweryKey key;


    public NamedDrunkEvent(String name, int alcoholRequirement, int toxinsRequirement, int probabilityWeight) {
        this.name = name;
        this.alcoholRequirement = alcoholRequirement;
        this.toxinsRequirement = toxinsRequirement;
        this.probabilityWeight = probabilityWeight;

        this.key = BreweryKey.parse(name.toLowerCase(Locale.ROOT));
    }


    @Override
    public int alcoholRequirement() {
        return alcoholRequirement;
    }

    @Override
    public int toxinsRequirement() {
        return toxinsRequirement;
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
        return Component.translatable("tbp.events.types" + name.toLowerCase(Locale.ROOT));
    }

    @Override
    public int probabilityWeight() {
        return probabilityWeight;
    }


    public static NamedDrunkEvent fromKey(String key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return Registry.DRUNK_EVENT.get(BreweryKey.parse(key));
    }
}
