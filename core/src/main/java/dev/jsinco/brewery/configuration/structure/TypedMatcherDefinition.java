package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.Holder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TypedMatcherDefinition<T extends BreweryKeyed> implements BlockReplacement {

    private final Map<T, GenericMatcherDefinition> matchers;

    public TypedMatcherDefinition(Map<T, GenericMatcherDefinition> matchers) {
        this.matchers = matchers;
    }

    public Set<Holder.Material> convert(T t, String materialString, Holder.Material defaultValue) {
        GenericMatcherDefinition genericMatcherDefinition = matchers.get(t);
        if (genericMatcherDefinition == null) {
            return Set.of();
        }
        return genericMatcherDefinition.convert(materialString, defaultValue);
    }

    @Override
    public List<String> validate(Set<String> allowed) {
        List<String> output = new ArrayList<>();
        for (Map.Entry<T, GenericMatcherDefinition> entry : matchers.entrySet()) {
            entry.getValue().validate(allowed)
                    .stream()
                    .map(string -> entry.getKey().key().minimalized() + "." + string)
                    .forEach(output::add);
        }
        return output;
    }
}
