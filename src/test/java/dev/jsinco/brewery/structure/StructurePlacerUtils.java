package dev.jsinco.brewery.structure;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.mockbukkit.mockbukkit.block.data.BlockDataMock;
import org.mockbukkit.mockbukkit.block.data.StairsDataMock;
import org.mockbukkit.mockbukkit.block.data.WallSignDataMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

public class StructurePlacerUtils {

    public static void constructSmallOakBarrel(WorldMock worldMock) {
        StairsDataMock stairsDataMock1 = (StairsDataMock) BlockDataMock.mock(Material.OAK_STAIRS);
        stairsDataMock1.setFacing(BlockFace.EAST);
        stairsDataMock1.setHalf(Bisected.Half.TOP);
        worldMock.setBlockData(-3, 1, 2, stairsDataMock1);
        worldMock.setBlockData(-3, 1, 3, stairsDataMock1);
        worldMock.setBlockData(-3, 1, 4, stairsDataMock1);

        StairsDataMock stairsDataMock2 = (StairsDataMock) BlockDataMock.mock(Material.OAK_STAIRS);
        stairsDataMock2.setFacing(BlockFace.WEST);
        stairsDataMock2.setHalf(Bisected.Half.TOP);
        worldMock.setBlockData(-2, 1, 2, stairsDataMock2);
        worldMock.setBlockData(-2, 1, 3, stairsDataMock2);
        worldMock.setBlockData(-2, 1, 4, stairsDataMock2);

        StairsDataMock stairsDataMock3 = (StairsDataMock) BlockDataMock.mock(Material.OAK_STAIRS);
        stairsDataMock3.setFacing(BlockFace.EAST);
        stairsDataMock3.setHalf(Bisected.Half.BOTTOM);
        worldMock.setBlockData(-3, 2, 2, stairsDataMock3);
        worldMock.setBlockData(-3, 2, 3, stairsDataMock3);
        worldMock.setBlockData(-3, 2, 4, stairsDataMock3);

        StairsDataMock stairsDataMock4 = (StairsDataMock) BlockDataMock.mock(Material.OAK_STAIRS);
        stairsDataMock4.setFacing(BlockFace.WEST);
        stairsDataMock4.setHalf(Bisected.Half.BOTTOM);
        worldMock.setBlockData(-2, 2, 2, stairsDataMock4);
        worldMock.setBlockData(-2, 2, 3, stairsDataMock4);
        worldMock.setBlockData(-2, 2, 4, stairsDataMock4);

        WallSignDataMock wallSignDataMock = new WallSignDataMock(Material.OAK_WALL_SIGN);
        wallSignDataMock.setFacing(BlockFace.NORTH);
        worldMock.setBlockData(-3, 1, 1, wallSignDataMock);
    }
}
