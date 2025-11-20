package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.MaterialTag;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.thorinwasher.schem.Schematic;
import eu.okaeri.configs.OkaeriConfig;
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

public class BreweryStructure  {

    private final Schematic schem;
    @Getter
    private final EntryPoints entryPoints;
    @Getter
    private final String name;
    @Getter
    private final Meta meta;
    @Getter
    private final String schemFileName;

    /**
     * Construct a schem structure where all blocks can finalize the structure
     *
     * @param schem
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NotNull Schematic schem, @NotNull String name, Meta structureMeta, String schemFileName) {
        this(schem, new EntryPoints(computeEntryPoints(schem), false), name, structureMeta, schemFileName);
    }

    /**
     *
     * @param schem
     * @param origins
     * @param name
     * @param structureMeta
     */
    public BreweryStructure(@NotNull Schematic schem, @NotNull EntryPoints origins, @NotNull String name, Meta structureMeta, String schemFileName) {
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

    public record EntryPoints(List<Vector3i> entryPoints, boolean customDefinition) {
    }

    public record Meta(Map<StructureMeta<?>, Object> data) {
    }
}
