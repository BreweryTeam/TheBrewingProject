package dev.jsinco.brewery.api.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;

import java.util.List;

public interface DrunkState {

    DrunkState recalculate(long timeStamp);

    DrunkState addModifier(DrunkenModifier modifier, double value);

    double modifierValue(String modifierName);

    List<Pair<DrunkenModifier, Double>> additionalModifierData();

    DrunkState withPassOut(long kickedTimestamp);

    long timestamp();

    long kickedTimestamp();
}
