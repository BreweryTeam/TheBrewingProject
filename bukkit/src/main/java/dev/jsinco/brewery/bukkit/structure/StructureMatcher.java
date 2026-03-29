package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.configuration.structure.GenericBlockReplacement;
import dev.jsinco.brewery.configuration.structure.KeyedBlockReplacement;
import dev.jsinco.brewery.configuration.structure.StructureMatcherDefinition;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StructureMatcher {

    private String name;
    private @Nullable BreweryKey key;
    private Map<BlockType, Set<BlockType>> transformations;
    private Map<BlockType, List<String>> checkedBlockData;

    public StructureMatcher(String name, @Nullable BreweryKey key, Map<BlockType, Set<BlockType>> transformations, Map<BlockType, List<String>> checkedBlockData) {
        this.name = name;
        this.key = key;
        this.transformations = transformations;
        this.checkedBlockData = checkedBlockData;
    }

    public String getName() {
        return name;
    }

    public BreweryKey typeKey() {
        return this.key;
    }

    public Set<BlockType> dumpBlockTypes() {
        return transformations
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public boolean matches(BlockData expected, BlockData actual) {
        String expectedString = expected.getAsString();
        List<String> allowedBlockData = checkedBlockData.get(expected.getMaterial().asBlockType());
        if (allowedBlockData != null) {
            final String expectedStringFinal = expectedString;
            expectedString = "[" + allowedBlockData
                    .stream()
                    .map(allowed -> Pattern.compile(allowed + " *= *([^=,\\]]+)"))
                    .map(pattern -> pattern.matcher(expectedStringFinal))
                    .filter(Matcher::find)
                    .map(Matcher::group)
                    .collect(Collectors.joining(","))
                    + "]";
        }
        final String expectedStringFinal = expectedString;
        return transformations.get(expected.getMaterial().asBlockType())
                .stream()
                .anyMatch(transformedExpected -> actual.matches(transformedExpected.createBlockData(expectedStringFinal)));
    }

    public static List<StructureMatcher> getMatchers(StructureMatcherDefinition structureMatcherDefinition) {
        Map<String, Holder.Material> materialDefinitions = structureMatcherDefinition.replacementMaterial;
        Map<String, List<String>> blockDataFilter = structureMatcherDefinition.blockDataFilter;
        Map<BlockType, List<String>> convertedBlockDataFilter = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : blockDataFilter.entrySet()) {
            Material converted = BukkitAdapter.toMaterial(materialDefinitions.get(entry.getKey()));
            Preconditions.checkArgument(converted != null, "Invalid material for replacement: " + entry.getKey());
            BlockType convertedType = converted.asBlockType();
            Preconditions.checkArgument(convertedType != null, "Material not a block: " + converted);
            convertedBlockDataFilter.put(convertedType, entry.getValue());
        }
        if (structureMatcherDefinition.blockTransformation instanceof KeyedBlockReplacement keyed) {
            return keyed.listAlternatives()
                    .stream()
                    .map(key -> compile(
                            structureMatcherDefinition.name,
                            key,
                            materialDefinitions,
                            convertedBlockDataFilter,
                            (string, defaultValue) -> keyed.convert(key, string, defaultValue)
                    ))
                    .toList();
        } else if (structureMatcherDefinition.blockTransformation instanceof GenericBlockReplacement generic) {
            return List.of(compile(structureMatcherDefinition.name, null, materialDefinitions, convertedBlockDataFilter, generic::convert));
        } else {
            throw new IllegalStateException("Unknown block replacement type");
        }
    }

    private static StructureMatcher compile(String name, @Nullable BreweryKey key, Map<String, Holder.Material> materialDefinitions,
                                            Map<BlockType, List<String>> blockDataFilter, BiFunction<String, Holder.Material, Set<Holder.Material>> convert) {
        Map<BlockType, Set<BlockType>> transforms = new HashMap<>();
        for (Map.Entry<String, Holder.Material> entry : materialDefinitions.entrySet()) {
            Material toReplace = BukkitAdapter.toMaterial(entry.getValue());
            Preconditions.checkArgument(toReplace != null, "Invalid material '" + entry.getValue().value() + "' to replace: Expected a block type key");
            BlockType toReplaceType = toReplace.asBlockType();
            Preconditions.checkArgument(toReplaceType != null, "Material is not a block: " + toReplace);
            Set<BlockType> replacements = convert.apply(entry.getKey(), entry.getValue())
                    .stream()
                    .map(BukkitAdapter::toMaterial)
                    .filter(Objects::nonNull)
                    .map(Material::asBlockType)
                    .collect(Collectors.toSet());
            transforms.put(toReplaceType, replacements);
        }
        return new StructureMatcher(name, key, transforms, blockDataFilter);
    }
}
