package dev.jsinco.brewery.bukkit.structure;

import dev.jsinco.brewery.bukkit.structure.serializer.*;
import dev.jsinco.brewery.configuration.OkaeriSerdesPackBuilder;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void fromJson_names(String pathString) throws StructureReadException, IOException, URISyntaxException {
        String structureName = pathString.replace("/structures/", "").replace(".json", "");
        URL url = PlacedBreweryStructure.class.getResource(pathString);
        Path path = Paths.get(url.toURI());
        OkaeriSerdesPack pack = new OkaeriSerdesPackBuilder()
                .add(new BreweryVectorSerializer())
                .add(new BreweryVectorListSerializer())
                .add(new MaterialHolderSerializer())
                .add(new MaterialTagSerializer())
                .add(new StructureMetaSerializer())
                .add(new Vector3iSerializer())
                .build();
        BreweryStructure structure = ConfigManager.create(BreweryStructureConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(), pack);
            it.withBindFile(path);
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        }).toStructure(path);
        assertEquals(structureName, structure.getName());
    }

    static Stream<Arguments> getSchemFormatPaths() {
        return Stream.of("/structures/large_barrel.json", "/structures/small_barrel.json").map(Arguments::of);
    }
}