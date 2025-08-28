package dev.jsinco.brewery.effect;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.effect.DrunkState;

import java.util.List;
import java.util.Map;

public record DrunkStateImpl(long timestamp,
                             long kickedTimestamp, Map<DrunkenModifier, Double> modifiers) implements DrunkState {

    public DrunkStateImpl recalculate(long timestamp) {
        if (timestamp < this.timestamp) {
            return new DrunkStateImpl(this.timestamp, this.kickedTimestamp, modifiers);
        }
        int diff = (int) (timestamp - this.timestamp);
        ImmutableMap.Builder<DrunkenModifier, Double> newDrunkenModifiers = new ImmutableMap.Builder<>();
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            double decrementTime = entry.getKey().decrementTime().evaluate(modifiers);
            double value;
            if (decrementTime == 0D) {
                value = 0D;
            } else if (decrementTime == -1D) {
                value = entry.getValue();
            } else {
                value = entry.getValue() - diff / decrementTime;
            }
            newDrunkenModifiers.put(entry.getKey(), Math.max(0, Math.min(value, 100)));
        }
        return new DrunkStateImpl(timestamp, this.kickedTimestamp, newDrunkenModifiers.build());
    }

    public DrunkStateImpl addModifier(DrunkenModifier modifier, double value) {
        ImmutableMap.Builder<DrunkenModifier, Double> newModifiers = new ImmutableMap.Builder<>();
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            newModifiers.put(entry.getKey(), entry.getKey().equals(modifier) ? value : entry.getValue());
        }
        return new DrunkStateImpl(
                this.timestamp,
                this.kickedTimestamp,
                newModifiers.build()
        );
    }

    @Override
    public double modifierValue(String modifierName) {
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            if (entry.getKey().name().equals(modifierName)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Unknown modifier: " + modifierName);
    }

    @Override
    public List<Pair<DrunkenModifier, Double>> additionalModifierData() {
        return modifiers.entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .filter(pair -> pair.first().defaultValue() != pair.second())
                .toList();
    }

    public DrunkStateImpl withPassOut(long kickedTimestamp) {
        return new DrunkStateImpl(this.timestamp, kickedTimestamp, modifiers);
    }
}
