package dev.jsinco.brewery.bukkit.structure;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Fence;
import org.mockbukkit.mockbukkit.block.data.BlockDataMockFactory;
import org.mockbukkit.mockbukkit.block.data.StairsDataMock;
import org.mockbukkit.mockbukkit.block.data.WallSignDataMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class StructurePlacerUtils {

    public static void constructSmallOakBarrel(WorldMock worldMock) {
        StairsDataMock stairsDataMock1 = (StairsDataMock) BlockDataMockFactory.mock(Material.SPRUCE_STAIRS);
        stairsDataMock1.setFacing(BlockFace.EAST);
        stairsDataMock1.setHalf(Bisected.Half.TOP);
        worldMock.setBlockData(-3, 1, 2, stairsDataMock1);
        worldMock.setBlockData(-3, 1, 3, stairsDataMock1);
        worldMock.setBlockData(-3, 1, 4, stairsDataMock1);

        StairsDataMock stairsDataMock2 = (StairsDataMock) BlockDataMockFactory.mock(Material.SPRUCE_STAIRS);
        stairsDataMock2.setFacing(BlockFace.WEST);
        stairsDataMock2.setHalf(Bisected.Half.TOP);
        worldMock.setBlockData(-2, 1, 2, stairsDataMock2);
        worldMock.setBlockData(-2, 1, 3, stairsDataMock2);
        worldMock.setBlockData(-2, 1, 4, stairsDataMock2);

        StairsDataMock stairsDataMock3 = (StairsDataMock) BlockDataMockFactory.mock(Material.SPRUCE_STAIRS);
        stairsDataMock3.setFacing(BlockFace.EAST);
        stairsDataMock3.setHalf(Bisected.Half.BOTTOM);
        worldMock.setBlockData(-3, 2, 2, stairsDataMock3);
        worldMock.setBlockData(-3, 2, 3, stairsDataMock3);
        worldMock.setBlockData(-3, 2, 4, stairsDataMock3);

        StairsDataMock stairsDataMock4 = (StairsDataMock) BlockDataMockFactory.mock(Material.SPRUCE_STAIRS);
        stairsDataMock4.setFacing(BlockFace.WEST);
        stairsDataMock4.setHalf(Bisected.Half.BOTTOM);
        worldMock.setBlockData(-2, 2, 2, stairsDataMock4);
        worldMock.setBlockData(-2, 2, 3, stairsDataMock4);
        worldMock.setBlockData(-2, 2, 4, stairsDataMock4);

        WallSignDataMock wallSignDataMock = new WallSignDataMock(Material.SPRUCE_WALL_SIGN);
        wallSignDataMock.setFacing(BlockFace.NORTH);
        worldMock.setBlockData(-3, 1, 1, wallSignDataMock);
    }

    public static void constructBambooDistillery(WorldMock worldMock) {
        Fence bamboo1 = (Fence) BlockDataMockFactory.mock(Material.BAMBOO_FENCE);
        bamboo1.setFace(BlockFace.EAST, true);
        Fence bamboo2 = (Fence) BlockDataMockFactory.mock(Material.BAMBOO_FENCE);
        bamboo1.setFace(BlockFace.WEST, true);
        worldMock.setBlockData(0, 1, 0, Material.DECORATED_POT.createBlockData());
        worldMock.setBlockData(0, 2, 0, Material.DECORATED_POT.createBlockData());
        worldMock.setBlockData(0, 3, 0, bamboo1);
        worldMock.setBlockData(1, 3, 0, bamboo2);
        worldMock.setBlockData(1, 2, 0, Material.BAMBOO_FENCE.createBlockData());
        worldMock.setBlockData(1, 1, 0, Material.DECORATED_POT.createBlockData());
    }

    public static BreweryStructure matchingStructure() throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.json");
        return StructureReader.fromJson(Paths.get(url.toURI()));
    }
}
