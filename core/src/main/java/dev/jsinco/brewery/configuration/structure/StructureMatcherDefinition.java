package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.util.Holder;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureMatcherDefinition extends OkaeriConfig {

    @CustomKey("name")
    public final @Nullable String name = null;

    @CustomKey("replacement-material")
    public final Map<String, Holder.Material> replacementMaterial = Map.of();

    @CustomKey("block-data-filter")
    public final Map<String, List<String>> blockDataFilter = Map.of();

    @CustomKey("block-transformation")
    public final BlockReplacement blockTransformation = new GenericMatcherDefinition(Map.of());

    /**
     * @return A list of keys that are invalid
     */
    public List<String> findInvalidKeys() {
        List<String> output = new ArrayList<>();
        Set<String> allowed = replacementMaterial.keySet();
        for (String materialPattern : blockDataFilter.keySet()) {
            if (!allowed.contains(materialPattern)) {
                output.add("block-data-filter." + materialPattern);
            }
        }
        blockTransformation.validate(allowed)
                .stream()
                .map(string -> "block-transformation." + string)
                .forEach(allowed::add);
        return output;
    }
}
