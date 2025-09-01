package dev.jsinco.brewery.api.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;

import java.util.List;
import java.util.Map;

public interface DrunkState {

    DrunkState recalculate(long timeStamp);

    DrunkState setModifier(DrunkenModifier modifier, double value);

    DrunkState withModifiers(Map<DrunkenModifier, Double> modifiers);

    double modifierValue(String modifierName);

    List<Pair<DrunkenModifier, Double>> additionalModifierData();

    DrunkState withPassOut(long kickedTimestamp);

    long timestamp();

    long kickedTimestamp();

    Map<DrunkenModifier, Double> modifiers();

    Pair<DrunkState, Boolean> cascadeModifier(DrunkenModifier modifier, double value);
}
