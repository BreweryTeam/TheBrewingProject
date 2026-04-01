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
    @Nullable
    private String name = null;

    @CustomKey("replacement-material")
    private Map<String, Holder.Material> replacementMaterial = Map.of();

    @CustomKey("block-data-filter")
    private Map<String, List<String>> blockDataFilter = Map.of();

    @CustomKey("block-transformation")
    private BlockReplacement blockTransformation = new GenericBlockReplacement(Map.of());

    /**
     * @return A list of keys that are invalid
     */
    public List<String> findInvalidKeys() {
        List<String> output = new ArrayList<>();
        Set<String> allowed = replacementMaterial().keySet();
        for (String materialPattern : blockDataFilter().keySet()) {
            if (!allowed.contains(materialPattern)) {
                output.add("block-data-filter." + materialPattern);
            }
        }
        blockTransformation().validate(allowed)
                .stream()
                .map(string -> "block-transformation." + string)
                .forEach(output::add);
        return output;
    }

    public @Nullable String name() {
        return name;
    }

    public Map<String, Holder.Material> replacementMaterial() {
        return replacementMaterial;
    }

    public Map<String, List<String>> blockDataFilter() {
        return blockDataFilter;
    }

    public BlockReplacement blockTransformation() {
        return blockTransformation;
    }
}
