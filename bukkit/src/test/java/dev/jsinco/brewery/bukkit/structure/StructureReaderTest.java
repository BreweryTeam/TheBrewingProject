package dev.jsinco.brewery.bukkit.structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockBukkitExtension.class)
class StructureReaderTest {

    @MockBukkitInject
    ServerMock serverMock;
    WorldMock worldMock;

    @BeforeEach
    void setUp() {
        this.worldMock = serverMock.addSimpleWorld("test_world");
    }

    @ParameterizedTest
    @MethodSource("getSchemFormatPaths")
    void fromJson_names(String path) throws StructureReadException, IOException {
        String structureName = path.replace("/structures/", "").replace(".json", "");
        BreweryStructure structures = StructureReader.fromInternalResourceJson(path);
        assertEquals(structureName, structures.getName());
    }

    static Stream<Arguments> getSchemFormatPaths() {
        return Stream.of("/structures/large_barrel.json", "/structures/small_barrel.json").map(Arguments::of);
    }
}