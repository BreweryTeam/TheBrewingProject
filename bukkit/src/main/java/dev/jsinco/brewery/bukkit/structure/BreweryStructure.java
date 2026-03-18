package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BreweryStructure {

    private final Schematic schem;
    private final EntryPoints entryPoints;
    private final String name;
    private final Meta meta;
    private final String schemFileName;

    /**
     * Construct a schem structure where all blocks can finalize the structure
     *
     * @param schem
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NonNull Schematic schem, @NonNull String name, Meta structureMeta, String schemFileName) {
        this(schem, new EntryPoints(computeEntryPoints(schem), false), name, structureMeta, schemFileName);
    }

    /**
     *
     * @param schem
     * @param origins
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NonNull Schematic schem, @NonNull EntryPoints origins, @NonNull String name, Meta structureMeta, String schemFileName) {
        this.schem = Objects.requireNonNull(schem);
        this.entryPoints = origins;
        this.name = Objects.requireNonNull(name);
        this.meta = Objects.requireNonNull(structureMeta);
        this.schemFileName = schemFileName;
    }

    private static List<Vector3i> computeEntryPoints(Schematic schem) {
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
        for (Vector3i structureEntryPoint : entryPoints.entryPoints()) {
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

    public <V> @Nullable V getMeta(StructureMeta<V> meta) {
        return (V) this.meta.data().get(meta);
    }

    public <V> V getMetaOrDefault(StructureMeta<V> meta, V defaultValue) {
        return (V) this.meta.data().getOrDefault(meta, defaultValue);
    }

    public boolean hasMeta(StructureMeta<?> metaKey) {
        return meta.data().containsKey(metaKey);
    }

    public EntryPoints getEntryPoints() {
        return this.entryPoints;
    }

    public String getName() {
        return this.name;
    }

    public Meta getMeta() {
        return this.meta;
    }

    public String getSchemFileName() {
        return this.schemFileName;
    }

    public record EntryPoints(List<Vector3i> entryPoints, boolean customDefinition) {
    }

    public record Meta(Map<StructureMeta<?>, Object> data) {
    }
}
