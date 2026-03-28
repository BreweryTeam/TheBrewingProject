package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class KeyedBlockReplacement implements BlockReplacement {

    private final Map<BreweryKey, GenericBlockReplacement> matchers;

    public KeyedBlockReplacement(Map<BreweryKey, GenericBlockReplacement> matchers) {
        this.matchers = matchers;
    }

    public Set<Holder.Material> convert(BreweryKey key, String materialString, Holder.Material defaultValue) {
        GenericBlockReplacement genericBlockReplacement = matchers.get(key);
        if (genericBlockReplacement == null) {
            return Set.of();
        }
        return genericBlockReplacement.convert(materialString, defaultValue);
    }

    public Collection<BreweryKey> listAlternatives() {
        return matchers.keySet();
    }

    @Override
    public List<String> validate(Set<String> allowed) {
        List<String> output = new ArrayList<>();
        for (Map.Entry<BreweryKey, GenericBlockReplacement> entry : matchers.entrySet()) {
            entry.getValue().validate(allowed)
                    .stream()
                    .map(string -> entry.getKey().minimalized() + "." + string)
                    .forEach(output::add);
        }
        return output;
    }
}
