package dev.jsinco.brewery.effect;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DrunkStateImpl(long timestamp,
                             long kickedTimestamp, Map<DrunkenModifier, Double> modifiers) implements DrunkState {

    public DrunkStateImpl {
        ImmutableMap.Builder<DrunkenModifier, Double> builder = new ImmutableMap.Builder<>();
        for (DrunkenModifier drunkenModifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            builder.put(drunkenModifier, drunkenModifier.sanitize(modifiers.getOrDefault(drunkenModifier, drunkenModifier.minValue())));
        }
        modifiers = builder.build();
    }

    public DrunkStateImpl(long timestamp, long kickedTimestamp) {
        this(timestamp, kickedTimestamp, Map.of());
    }

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
            newDrunkenModifiers.put(entry.getKey(), value);
        }
        return new DrunkStateImpl(timestamp, this.kickedTimestamp, newDrunkenModifiers.build());
    }

    @Override
    public DrunkStateImpl setModifier(DrunkenModifier modifier, double value) {
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
                .filter(pair -> pair.first().minValue() != pair.second())
                .toList();
    }

    public DrunkStateImpl withPassOut(long kickedTimestamp) {
        return new DrunkStateImpl(this.timestamp, kickedTimestamp, modifiers);
    }

    @Override
    public Pair<DrunkState, Boolean> cascadeModifier(DrunkenModifier modifierToAdd, double valueChange) {
        ImmutableMap.Builder<DrunkenModifier, Double> newModifiers = new ImmutableMap.Builder<>();
        Map<String, Double> variables = compileVariables(modifiers, modifierToAdd, valueChange);
        boolean cascadedOnSelf = false;
        for (Map.Entry<DrunkenModifier, Double> entry : modifiers.entrySet()) {
            double diff = entry.getKey().dependency().evaluate(variables);
            if (diff != 0D) {
                newModifiers.put(entry.getKey(), entry.getValue() + diff);
                if (modifierToAdd.equals(entry.getKey())) {
                    cascadedOnSelf = true;
                }
            } else {
                newModifiers.put(entry.getKey(), entry.getValue());
            }
        }
        return new Pair<>(new DrunkStateImpl(timestamp, kickedTimestamp, newModifiers.build()), cascadedOnSelf);
    }

    @Override
    public Map<String, Double> asVariables() {
        return compileVariables(this.modifiers, null, 0D);
    }

    @Override
    public Map<String, Double> asVariables(DrunkenModifier modifier, double valueChange) {
        return compileVariables(this.modifiers, modifier, valueChange);
    }

    public static Map<String, Double> compileVariables(Map<DrunkenModifier, Double> modifiers, @Nullable DrunkenModifier modifierToAdd, double valueChange) {
        Map<String, Double> output = new HashMap<>();
        for (DrunkenModifier modifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            Double value = modifiers.get(modifier);
            if (value == null) {
                output.put(modifier.name(), modifier.minValue());
                output.put("consumed_" + modifier.name(), 0D);
                continue;
            }
            output.put(modifier.name(), value);
            if (modifier.equals(modifierToAdd)) {
                output.put("consumed_" + modifierToAdd.name(), valueChange);
            } else {
                output.put("consumed_" + modifier.name(), 0D);
            }
        }
        return output;
    }
}
