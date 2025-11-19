package dev.jsinco.brewery.bukkit.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
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
import java.util.HashMap;
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
        assertThrows(IllegalArgumentException.class, () -> new BreweryStructure(schematic, "hello", new BreweryStructure.Meta(structureMeta), "hello.schem"));
    }

    @ParameterizedTest
    @MethodSource("getIncompleteMeta")
    void incompleteMeta(Map<StructureMeta<?>, Object> inputMeta, Map<StructureMeta<?>, Object> expectedMeta) throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        BreweryStructure breweryStructure = new BreweryStructure(schematic, "hello", new BreweryStructure.Meta(inputMeta), "hello.schem");
        expectedMeta.forEach((key, value) -> assertEquals(value, breweryStructure.getMeta(key)));
    }

    @ParameterizedTest
    @MethodSource("getFullMeta")
    void fullMeta(Map<StructureMeta<?>, Object> structureMeta) throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        assertDoesNotThrow(() -> new BreweryStructure(schematic, "hello", new BreweryStructure.Meta(structureMeta), "hello.schem"));
    }

    private BreweryStructure getOakBarrel() throws URISyntaxException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        return new BreweryStructure(schematic,
                new BreweryStructure.EntryPoints(List.of(new Vector3i(0, 0, 1)), true), "test_barrel",
                new BreweryStructure.Meta(Map.of(StructureMeta.INVENTORY_SIZE, 9,
                        StructureMeta.USE_BARREL_SUBSTITUTION, false,
                        StructureMeta.TYPE, StructureType.BARREL)
                ), "test_barrel.schem");
    }

    /**
     * Helper method to create a mutable map for testing BreweryStructure, also allows null values
     */
    private static <T, U> Map<T, U> mutableMapOf(Object... entries) {
        HashMap<T, U> map = new HashMap<>(Map.<T, U>of());
        for (int i = 0; i < entries.length; i += 2) {
            map.put((T) entries[i], (U) entries[i + 1]);
        }
        return map;
    }

    private static Stream<Arguments> getInvalidMeta() {
        return Stream.<Arguments>of(
                Arguments.of(mutableMapOf(StructureMeta.INVENTORY_SIZE, 9)),
                Arguments.of(mutableMapOf(
                        StructureMeta.TYPE, StructureType.BARREL,
                        StructureMeta.USE_BARREL_SUBSTITUTION, true,
                        StructureMeta.INVENTORY_SIZE, 14)
                )
        );
    }

    private static Stream<Arguments> getIncompleteMeta() {
        return Stream.of(
                Arguments.of(
                        mutableMapOf(
                                StructureMeta.TYPE, StructureType.BARREL
                        ),
                        mutableMapOf(
                                StructureMeta.INVENTORY_SIZE, 9,
                                StructureMeta.USE_BARREL_SUBSTITUTION, false,
                                StructureMeta.PROCESS_AMOUNT, null
                        )
                ),
                Arguments.of(
                        mutableMapOf(
                                StructureMeta.TYPE, StructureType.DISTILLERY
                        ), mutableMapOf( // don't check for everything, so the test doesn't break on meta changes
                                StructureMeta.INVENTORY_SIZE, 9,
                                StructureMeta.USE_BARREL_SUBSTITUTION, null,
                                StructureMeta.PROCESS_AMOUNT, 1
                        )
                )
        );
    }

    private static Stream<Arguments> getFullMeta() {
        return Stream.of(
                Arguments.of(mutableMapOf(
                        StructureMeta.TYPE, StructureType.DISTILLERY,
                        StructureMeta.INVENTORY_SIZE, 18,
                        StructureMeta.PROCESS_TIME, 0L,
                        StructureMeta.PROCESS_AMOUNT, 1
                )),
                Arguments.of(mutableMapOf(
                        StructureMeta.TYPE, StructureType.BARREL,
                        StructureMeta.INVENTORY_SIZE, 9,
                        StructureMeta.USE_BARREL_SUBSTITUTION, true)
                ));
    }
}