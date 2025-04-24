package dev.jsinco.brewery.bukkit.structure;

import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.structure.MultiblockStructure;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class PlacedBreweryStructure<H extends StructureHolder<H>> implements MultiblockStructure<H> {
    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
    private final BreweryStructure structure;
    private final Matrix3d transformation;
    private final Location worldOrigin;
    private final BreweryLocation unique;
    @Setter
    @Getter
    private @Nullable H holder = null;

    public PlacedBreweryStructure(BreweryStructure structure, Matrix3d transformation,
                                  Location worldOrigin) {
        this.structure = structure;
        this.transformation = transformation;
        this.worldOrigin = worldOrigin;
        this.unique = compileUnique();
    }

    public static <T, H extends StructureHolder<H>> Optional<Pair<PlacedBreweryStructure<H>, T>> findValid(BreweryStructure structure, Location worldOrigin, BlockDataMatcher<T> blockDataMatcher, T[] types) {
        for (Matrix3d transformation : ALLOWED_TRANSFORMATIONS) {
            for (T type : types) {
                Optional<Location> possibleOrigin = structure.findValidOrigin(transformation, worldOrigin, blockDataMatcher, type);
                if (possibleOrigin.isPresent()) {
                    return possibleOrigin
                            .map(origin -> new Pair<>(new PlacedBreweryStructure<>(structure, transformation, origin), type));
                }
            }
        }
        return Optional.empty();
    }

    public List<BreweryLocation> positions() {
        return structure.getExpectedBlocks(transformation, worldOrigin)
                .keySet()
                .stream()
                .map(location -> new BreweryLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getUID()))
                .toList();
    }

    @Override
    public BreweryLocation getUnique() {
        return unique;
    }

    private BreweryLocation compileUnique() {
        List<BreweryLocation> positions = new ArrayList<>(positions());
        positions.sort(this::comparePositions);
        return positions.getFirst();
    }

    private static List<Matrix3d> compileAllowedTransformations() {
        List<Matrix3d> output = new ArrayList<>();
        Matrix3d transformation = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()));
        }
        transformation.reflect(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            output.add(transformation.rotate(Math.PI / 2 * i, 0, 1, 0, new Matrix3d()));
        }
        return List.copyOf(output);
    }

    private int comparePositions(BreweryLocation breweryLocation, BreweryLocation breweryLocation1) {
        if (breweryLocation.y() > breweryLocation1.y()) {
            return -1;
        }
        if (breweryLocation.x() > breweryLocation1.x()) {
            return -1;
        }
        if (breweryLocation.z() > breweryLocation1.z()) {
            return -1;
        }
        return 0;
    }
}
