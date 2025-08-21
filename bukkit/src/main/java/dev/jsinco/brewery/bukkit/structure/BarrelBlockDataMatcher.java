package dev.jsinco.brewery.bukkit.structure;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ImmutableSet;
import dev.jsinco.brewery.breweries.BarrelType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class BarrelBlockDataMatcher implements BlockDataMatcher<BarrelType> {

    public static final BarrelBlockDataMatcher INSTANCE = new BarrelBlockDataMatcher();

    @Override
    public boolean matches(BlockData actual, BlockData expected, BarrelType matcherType) {
        if (matcherType == BarrelType.ANY) {
            throw new IllegalArgumentException("Can not match to: ANY");
        }
        Material expectedMaterial = expected.getMaterial();
        if (Tag.WOODEN_FENCES.isTagged(expectedMaterial)) {
            if (!Tag.WOODEN_FENCES.isTagged(actual.getMaterial())) {
                return false;
            }
            return fenceMatches((Fence) actual, (Fence) expected, matcherType);
        }
        if (expected instanceof Stairs expectedStairs) {
            if (!(actual instanceof Stairs actuallStairs)) {
                return false;
            }
            return stairMatches(actuallStairs, expectedStairs, matcherType);
        }
        if (BarrelType.COPPER == matcherType) {
            return MaterialTags.CUT_COPPER_BLOCKS.isTagged(actual.getMaterial());
        }
        return materialMatches(actual, expected, matcherType);
    }

    @Override
    public Set<Material> findStructureMaterials(BarrelType matcherType, BreweryStructure structure) {
        if (matcherType == BarrelType.COPPER) {
            ImmutableSet.Builder<Material> output = new ImmutableSet.Builder<>();
            output.addAll(MaterialTags.CUT_COPPER_BLOCKS.getValues());
            output.addAll(MaterialTags.CUT_COPPER_STAIRS.getValues());
            output.addAll(Tag.FENCES.getValues());
            return output.build();
        }
        return structure.getPalette()
                .stream().map(blockData -> findSubstitution(blockData, matcherType))
                .collect(Collectors.toSet());
    }

    private @Nullable Material findSubstitution(BlockData expected, BarrelType matcherType) {
        String materialString = expected.getMaterial().getKey().toString();
        NamespacedKey key = NamespacedKey.fromString(materialString.replaceAll("oak", matcherType.name().toLowerCase(Locale.ROOT)));
        return Registry.MATERIAL.get(key);
    }

    private boolean stairMatches(Stairs actualStairs, Stairs expectedStairs, BarrelType matcherType) {
        if (actualStairs.isWaterlogged() != expectedStairs.isWaterlogged()) {
            return false;
        }
        if (actualStairs.getFacing() != expectedStairs.getFacing()) {
            return false;
        }
        if (actualStairs.getHalf() != expectedStairs.getHalf()) {
            return false;
        }
        if (matcherType == BarrelType.COPPER) {
            return MaterialTags.CUT_COPPER_STAIRS.isTagged(actualStairs.getMaterial());
        }
        return materialMatches(actualStairs, expectedStairs, matcherType);
    }

    private boolean fenceMatches(Fence actual, Fence expected, BarrelType matcherType) {
        if (actual.isWaterlogged() != expected.isWaterlogged()) {
            return false;
        }
        if (matcherType == BarrelType.COPPER) {
            return true;
        }
        return materialMatches(actual, expected, matcherType);
    }

    private boolean materialMatches(BlockData actual, BlockData expected, BarrelType matcherType) {
        Material substituted = findSubstitution(expected, matcherType);
        if (substituted == null) {
            throw new IllegalArgumentException("Could not find substitution for '" + matcherType + "' with material: " + expected.getMaterial());
        }
        return substituted == actual.getMaterial();
    }


}
