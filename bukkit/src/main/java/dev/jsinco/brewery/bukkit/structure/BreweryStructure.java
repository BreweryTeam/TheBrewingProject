package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.thorinwasher.schem.Schematic;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;

public class BreweryStructure {

    private final Schematic schem;
    private final List<Vector3i> entryPoints;
    @Getter
    private final String name;
    private final Map<StructureMeta< ?>, Object> structureMeta;

    /**
     * Construct a schem structure where all blocks can finalize the structure (less performant)
     *
     * @param schem
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NotNull Schematic schem, @NotNull String name, Map<StructureMeta< ?>, Object> structureMeta) {
        this(schem, computeOrigins(schem), name, structureMeta);
    }

    /**
     * Construct a schem structure where only one block can finalize the structure
     *
     * @param schem
     * @param origins
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NotNull Schematic schem, @NotNull List<Vector3i> origins, @NotNull String name, Map<StructureMeta< ?>, Object> structureMeta) {
        this.schem = Objects.requireNonNull(schem);
        this.entryPoints = origins;
        this.name = Objects.requireNonNull(name);
        this.structureMeta = Objects.requireNonNull(structureMeta);
        structureMeta.forEach((key, value) -> Preconditions.checkArgument(key.validator().test(value), "Invalid structure '" + name + "': value '" + value + "' is not allowed for meta: " + structureMeta));
        StructureType type = getMeta(StructureMeta.TYPE);
        Preconditions.checkArgument(type != null, "Invalid structure '" + name + "', missing meta: " + StructureMeta.TYPE);
        List<StructureMeta< ?>> missing = type.getMissingMandatory(structureMeta.keySet());
        Preconditions.checkArgument(missing.isEmpty(), "Invalid structure '" + name + "', missing meta: " + missing);
    }

    private static List<Vector3i> computeOrigins(Schematic schem) {
        List<Vector3i> vector3iList = new ArrayList<>();
        schem.apply(new Matrix3d(), (position, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            vector3iList.add(position);
        });
        return List.copyOf(vector3iList);
    }

    public <T> Optional<Location> findValidOrigin(Matrix3d transformation, Location entryPoint, BlockDataMatcher<T> blockDataMatcher, T matcherType) {
        Preconditions.checkNotNull(entryPoint.getWorld(), "World for entry point can not be null!");
        for (Vector3i structureEntryPoint : entryPoints) {
            Vector3d transformedEntryPoint = transformation.transform(new Vector3d(structureEntryPoint));
            Location worldOrigin = entryPoint.clone().subtract((int) transformedEntryPoint.x(), (int) transformedEntryPoint.y(), (int) transformedEntryPoint.z());
            if (matches(transformation, worldOrigin, blockDataMatcher, matcherType)) {
                return Optional.of(worldOrigin);
            }
        }
        return Optional.empty();
    }

    private <T> boolean matches(Matrix3d transformation, Location structureWorldOrigin, BlockDataMatcher<T> blockDataMatcher, T matcherType) {
        Map<Location, BlockData> expectedBlocks = getExpectedBlocks(transformation, structureWorldOrigin);
        for (Map.Entry<Location, BlockData> expected : expectedBlocks.entrySet()) {
            World world = expected.getKey().getWorld();
            if (!world.getWorldBorder().isInside(expected.getKey()) || world.getMinHeight() > expected.getKey().getBlockY() || world.getMaxHeight() <= expected.getKey().getBlockY()) {
                return false;
            }
            if (!blockDataMatcher.matches(expected.getKey().getBlock().getBlockData(), expected.getValue(), matcherType)) {
                return false;
            }
        }
        return true;
    }

    public Map<Location, BlockData> getExpectedBlocks(Matrix3d transformation, Location structureWorldOrigin) {
        Preconditions.checkNotNull(structureWorldOrigin.getWorld(), "World for world origin can not be null!");
        Map<Location, BlockData> output = new HashMap<>();

        schem.apply(transformation, (schematicSpacePosition, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            output.put(structureWorldOrigin.clone().add(schematicSpacePosition.x(), schematicSpacePosition.y(), schematicSpacePosition.z()), blockData);
        });
        return output;
    }

    public List<BlockData> getPalette() {
        return Arrays.asList(schem.palette());
    }

    public <V> @Nullable V getMeta(StructureMeta< V> meta) {
        return (V) structureMeta.get(meta);
    }

    public <V> V getMetaOrDefault(StructureMeta<V> meta, V defaultValue) {
        return (V) structureMeta.getOrDefault(meta, defaultValue);
    }
}
