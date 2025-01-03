package dev.jsinco.brewery.structure;

import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import org.bukkit.Location;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        BreweryStructure oakStructure = getOakStructure();
        StructurePlacerUtils.constructSmallOakBarrel(worldMock);
        assertTrue(PlacedBreweryStructure.findValid(oakStructure, new Location(worldMock, -3, 1, 1)).isPresent());
    }

    private BreweryStructure getOakStructure() throws URISyntaxException, IOException {
        URL url = PlacedBreweryStructure.class.getResource("/structures/test_barrel.schem");
        Schematic schematic = new SchematicReader().read(Paths.get(url.toURI()));
        return new BreweryStructure(schematic, new Vector3i(0, 0, 1));
    }
}