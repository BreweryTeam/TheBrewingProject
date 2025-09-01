package dev.jsinco.brewery.effect;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DrunkStateImpl(long timestamp,
                             long kickedTimestamp, Map<DrunkenModifier, Double> modifiers) implements DrunkState {

    public DrunkStateImpl recalculate(long timestamp) {
        if (timestamp < this.timestamp) {
            return new DrunkStateImpl(this.timestamp, this.kickedTimestamp, modifiers);
        }
        Map<String, Double> variables = compileVariables(modifiers, null, 0D);
        int diff = (int) (timestamp - this.timestamp);
        ImmutableMap.Builder<DrunkenModifier, Double> newDrunkenModifiers = new ImmutableMap.Builder<>();
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            double decrementTime = entry.getKey().decrementTime().evaluate(variables);
            double value;
            if (decrementTime == 0D) {
                value = 0D;
            } else if (decrementTime == -1D) {
                value = entry.getValue();
            } else {
                value = entry.getValue() - diff / decrementTime;
            }
            newDrunkenModifiers.put(entry.getKey(), sanitize(value));
        }
        return new DrunkStateImpl(timestamp, this.kickedTimestamp, newDrunkenModifiers.build());
    }

    @Override
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
    public DrunkStateImpl withModifiers(Map<DrunkenModifier, Double> modifiers) {
        return new DrunkStateImpl(timestamp, kickedTimestamp, modifiers);
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

    @Override
    public Pair<DrunkState, Boolean> cascadeModifier(DrunkenModifier modifierToAdd, double value) {
        ImmutableMap.Builder<DrunkenModifier, Double> newModifiers = new ImmutableMap.Builder<>();
        Map<String, Double> variables = compileVariables(modifiers, modifierToAdd, value);
        boolean cascadedOnSelf = false;
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            double diff = entry.getKey().dependency().evaluate(variables);
            if (diff != 0D) {
                newModifiers.put(entry.getKey(), sanitize(entry.getValue() + diff));
                if (modifierToAdd.equals(entry.getKey())) {
                    cascadedOnSelf = true;
                }
            }
        }
        return new Pair<>(new DrunkStateImpl(timestamp, kickedTimestamp, newModifiers.build()), cascadedOnSelf);
    }

    public static Map<String, Double> compileVariables(Map<DrunkenModifier, Double> modifiers, @Nullable DrunkenModifier modifierToAdd, double value) {
        Map<String, Double> output = new HashMap<>();
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            output.put(entry.getKey().name(), entry.getValue());
            if (entry.getKey().equals(modifierToAdd)) {
                output.put("d" + modifierToAdd.name(), value);
            } else {
                output.put("d" + entry.getKey().name(), entry.getValue());
            }
        }
        return output;
    }

    private double sanitize(double value) {
        return Math.max(0, Math.min(value, 100));
    }
}
