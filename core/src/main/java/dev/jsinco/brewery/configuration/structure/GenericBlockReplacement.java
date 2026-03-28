package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.Materials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GenericBlockReplacement implements BlockReplacement {

    private final Map<String, Materials> backing;

    public GenericBlockReplacement(Map<String, Materials> backing) {
        this.backing = backing;
    }

    public Set<Holder.Material> convert(String materialString, Holder.Material defaultValue) {
        Materials materials = backing.get(materialString);
        if (materials == null) {
            return Set.of(defaultValue);
        }
        return materials.values();
    }

    @Override
    public List<String> validate(Set<String> allowed) {
        List<String> output = new ArrayList<>();
        for (String pattern : backing.keySet()) {
            if (!allowed.contains(pattern)) {
                output.add(pattern);
            }
        }
        return output;
    }
}
