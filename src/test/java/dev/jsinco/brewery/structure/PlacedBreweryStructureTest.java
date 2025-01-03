package dev.jsinco.brewery.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.block.data.BlockDataMock;
import org.mockbukkit.mockbukkit.block.data.StairsDataMock;
import org.mockbukkit.mockbukkit.block.data.WallSignDataMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class PlacedBreweryStructureTest {
    @MockBukkitInject
    ServerMock serverMock;
    private WorldMock worldMock;

    @BeforeEach
    void setUp() {
        this.worldMock = serverMock.addSimpleWorld("hello_world");
    }

    @ParameterizedTest
    @MethodSource("insideSmallBarrel")
    void findValid_insideMatch(BlockVector blockVector) throws IOException, URISyntaxException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.json");
        Map<String, BreweryStructure> structures = StructureReader.fromJson(Paths.get(url.toURI()));
        BreweryStructure breweryStructure = null;
        for (Map.Entry<String, BreweryStructure> entry : structures.entrySet()) {
            if (entry.getKey().contains("oak")) {
                breweryStructure = entry.getValue();
                break;
            }
        }
        StructurePlacerUtils.constructSmallOakBarrel(worldMock);
        assertTrue(PlacedBreweryStructure.findValid(breweryStructure,
                new Location(worldMock, blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ())
        ).isPresent());
    }

    @ParameterizedTest
    @MethodSource("outsideSmallBarrel")
    void findValid_outsideNoMatch(BlockVector pos) throws IOException, URISyntaxException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.json");
        Map<String, BreweryStructure> structures = StructureReader.fromJson(Paths.get(url.toURI()));
        BreweryStructure breweryStructure = null;
        for (Map.Entry<String, BreweryStructure> entry : structures.entrySet()) {
            if (entry.getKey().contains("oak")) {
                breweryStructure = entry.getValue();
                break;
            }
        }
        StructurePlacerUtils.constructSmallOakBarrel(worldMock);
        assertFalse(PlacedBreweryStructure.findValid(breweryStructure, new Location(worldMock, pos.getX(), pos.getY(), pos.getZ())).isPresent());
    }

    @Test
    void getPositions() {
    }

    static Stream<Arguments> getSchemFormatPaths() {
        return Stream.of("/structures/large_barrel.json", "/structures/small_barrel.json")
                .map(StructureReaderTest.class::getResource)
                .map(url -> {
                    try {
                        return url.toURI();
                    } catch (URISyntaxException uriSyntaxException) {
                        throw new RuntimeException();
                    }
                })
                .map(Paths::get)
                .map(Arguments::of);
    }

    private static Set<BlockVector> getSmallBarrelBlocks() {
        return Set.of(new BlockVector(-3, 1, 2),
                new BlockVector(-3, 1, 3),
                new BlockVector(-3, 1, 4),
                new BlockVector(-2, 1, 2),
                new BlockVector(-2, 1, 3),
                new BlockVector(-2, 1, 4),
                new BlockVector(-3, 2, 2),
                new BlockVector(-3, 2, 3),
                new BlockVector(-3, 2, 4),
                new BlockVector(-2, 2, 2),
                new BlockVector(-2, 2, 3),
                new BlockVector(-2, 2, 4),
                new BlockVector(-3, 1, 1));
    }

    public static Stream<Arguments> insideSmallBarrel() {
        return getSmallBarrelBlocks().stream().map(Arguments::of);
    }

    public static Stream<Arguments> outsideSmallBarrel() {
        List<BlockVector> blockVectorArrayList = new ArrayList<>();
        Set<BlockVector> banned = getSmallBarrelBlocks();
        for (int x = -7; x < 7; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = -4; z < 10; z++) {
                    BlockVector blockVector = new BlockVector(x, y, z);
                    if (!banned.contains(blockVector)) {
                        blockVectorArrayList.add(blockVector);
                    }
                }
            }
        }
        return blockVectorArrayList.stream().map(Arguments::of);
    }

}