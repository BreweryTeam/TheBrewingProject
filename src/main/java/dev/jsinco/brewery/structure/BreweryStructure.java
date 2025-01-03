package dev.jsinco.brewery.structure;

import com.google.common.base.Preconditions;
import dev.thorinwasher.schem.BlockUtil;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;

public class BreweryStructure {

    private final Schematic schem;
    private final List<Vector3i> origins;

    /**
     * Construct a schem structure where all blocks can finalize the structure (less performant)
     *
     * @param schem
     */
    public BreweryStructure(Schematic schem) {
        this.schem = schem;
        this.origins = computeOrigins(schem);
    }

    /**
     * Construct a schem structure where only one block can finalize the structure
     *
     * @param schem
     * @param origin
     */
    public BreweryStructure(Schematic schem, Vector3i origin) {
        this.schem = schem;
        this.origins = List.of(origin);
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

    public Optional<Vector3i> findValidOrigin(Matrix3d transformation, Location structureWorldOrigin) {
        Preconditions.checkNotNull(structureWorldOrigin.getWorld(), "World for world origin can not be null!");
        for (Vector3i origin : origins) {
            if (matches(transformation, structureWorldOrigin, origin)) {
                return Optional.of(origin);
            }
        }
        return Optional.empty();
    }

    private boolean matches(Matrix3d transformation, Location structureWorldOrigin, Vector3i origin) {
        Map<Location, BlockData> expectedBlocks = getExpectedBlocks(transformation, structureWorldOrigin, origin);
        for (Map.Entry<Location, BlockData> expected : expectedBlocks.entrySet()) {
            World world = expected.getKey().getWorld();
            if (!world.getWorldBorder().isInside(expected.getKey())
                    || world.getMinHeight() > expected.getKey().getBlockY()
                    || world.getMaxHeight() <= expected.getKey().getBlockY()) {
                return false;
            }
            if (!expected.getKey().getBlock().getBlockData().equals(expected.getValue())) {
                return false;
            }
        }
        return true;
    }

    private Vector3d transform(Matrix3d transformation, Location structureWorldOrigin, Vector3i schematicSpacePosition, Vector3i origin) {
        Vector3i vector = schematicSpacePosition.sub(origin, new Vector3i());
        return transformation.transform(new Vector3d(vector)).add(structureWorldOrigin.getX(), structureWorldOrigin.getY(), structureWorldOrigin.getZ());
    }

    public Map<Location, BlockData> getExpectedBlocks(Matrix3d transformation, Location structureWorldOrigin, Vector3i origin) {
        Preconditions.checkNotNull(structureWorldOrigin.getWorld(), "World for world origin can not be null!");
        Map<Location, BlockData> output = new HashMap<>();
        World world = structureWorldOrigin.getWorld();

        schem.apply(new Matrix3d(), (schematicSpacePosition, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            Vector3d transformedVector = transform(transformation, structureWorldOrigin, schematicSpacePosition, origin);
            BlockData transformedBlockData = BlockUtil.transformBlockData(blockData, transformation);
            output.put(new Location(world, transformedVector.x(), transformedVector.y(), transformedVector.z()), transformedBlockData);
        });
        return output;
    }

    public List<BlockData> getPalette() {
        return Arrays.asList(schem.palette());
    }

    private Vector3i toVector3i(Vector3d vector3d) {
        return new Vector3i((int) vector3d.x(), (int) vector3d.y(), (int) vector3d.z());
    }
}
