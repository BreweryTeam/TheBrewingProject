package dev.jsinco.brewery.bukkit.structure;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import org.bukkit.Location;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockBukkitExtension.class)
class BreweryStructureTest {

    @MockBukkitInject
    ServerMock serverMock;
    private WorldMock worldMock;

    @BeforeEach
    void setUp() {
        this.worldMock = serverMock.addSimpleWorld("hello_world");
    }

    @Test
    void findValidOrigin() throws URISyntaxException, IOException {
        BreweryStructure oakStructure = getOakBarrel();
        StructurePlacerUtils.constructSmallOakBarrel(worldMock);
        assertTrue(PlacedBreweryStructure.findValid(oakStructure, new Location(worldMock, -3, 1, 1), BarrelBlockDataMatcher.INSTANCE, BarrelType.PLACEABLE_TYPES).isPresent());
    }

    @ParameterizedTest
    @MethodSource("getInvalidMeta")
    void invalidMeta(Map<StructureMeta<?>, Object> structureMeta) throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        assertThrows(IllegalArgumentException.class, () -> new BreweryStructure(schematic, "hello", structureMeta));
    }

    @ParameterizedTest
    @MethodSource("getValidMeta")
    void validMeta(Map<StructureMeta<?>, Object> structureMeta) throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        assertDoesNotThrow(() -> new BreweryStructure(schematic, "hello", structureMeta));
    }

    private BreweryStructure getOakBarrel() throws URISyntaxException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        return new BreweryStructure(schematic, List.of(new Vector3i(0, 0, 1)), "test_barrel",
                Map.of(StructureMeta.INVENTORY_SIZE, 9,
                        StructureMeta.USE_BARREL_SUBSTITUTION, false,
                        StructureMeta.TYPE, StructureType.BARREL));
    }

    private static Stream<Arguments> getInvalidMeta() {
        return Stream.of(Arguments.of(Map.of()),
                Arguments.of(Map.of(StructureMeta.INVENTORY_SIZE, 9)),
                Arguments.of(Map.of(StructureMeta.TYPE, StructureType.BARREL)),
                Arguments.of(Map.of(
                        StructureMeta.TYPE, StructureType.BARREL,
                        StructureMeta.USE_BARREL_SUBSTITUTION, true,
                        StructureMeta.INVENTORY_SIZE, 14)
                ),
                Arguments.of(Map.of(StructureMeta.TYPE, StructureType.DISTILLERY))
        );
    }

    private static Stream<Arguments> getValidMeta() {
        return Stream.of(
                Arguments.of(Map.of(
                        StructureMeta.TYPE, StructureType.DISTILLERY,
                        StructureMeta.INVENTORY_SIZE, 18,
                        StructureMeta.TAGGED_MATERIAL, "decorated_pot",
                        StructureMeta.PROCESS_TIME, 0L,
                        StructureMeta.PROCESS_AMOUNT, 1
                )),
                Arguments.of(Map.of(
                        StructureMeta.TYPE, StructureType.BARREL,
                        StructureMeta.INVENTORY_SIZE, 9,
                        StructureMeta.USE_BARREL_SUBSTITUTION, true)
        ));
    }
}