package dev.jsinco.brewery.structure;

import org.bukkit.Location;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

public record PlacedBreweryStructure(BreweryStructure structure, Matrix3d transformation,
                                     Location worldOrigin, Vector3i structureOrigin) {

    public static Optional<PlacedBreweryStructure> findValid(BreweryStructure structure, Location worldOrigin) {
        Matrix3d transformation = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            transformation.rotate(Math.PI / 2, 0, 1, 0);
            Optional<Vector3i> possibleOrigin = structure.findValidOrigin(transformation, worldOrigin);
            if (possibleOrigin.isPresent()) {
                return possibleOrigin.map(origin -> new PlacedBreweryStructure(structure, transformation, worldOrigin, origin));
            }
        }
        transformation.reflect(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            transformation.rotate(Math.PI / 2, 0, 1, 0);
            Optional<Vector3i> possibleOrigin = structure.findValidOrigin(transformation, worldOrigin);
            if (possibleOrigin.isPresent()) {
                return possibleOrigin.map(origin -> new PlacedBreweryStructure(structure, transformation, worldOrigin, origin));
            }
        }
        return Optional.empty();
    }

    public List<Location> getPositions() {
        return List.copyOf(structure.getExpectedBlocks(transformation, worldOrigin, structureOrigin)
                .keySet());
    }
}
