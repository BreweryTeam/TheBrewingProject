package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.CollectionUtil;
import dev.jsinco.brewery.util.FileUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.persistence.PersistentDataContainerMock;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrewingStepPdcTypeTest {

    private static final BrewingStepPdcType BREWING_STEP = new BrewingStepPdcType(true);

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setup() throws IOException {
        MockBukkit.mock(new TBPServerMock());
        TheBrewingProject plugin = MockBukkit.load(TheBrewingProject.class);

        // MockBukkit.loadWithConfig does not work with our Okaeri config setup
        // So we have to copy the config manually as a hackfix
        InputStream config = getClass().getResourceAsStream("/config/dummy_encryption_config.yml");
        Files.copy(config, tempDir.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
        Config.load(tempDir.toFile(), plugin.serializers());
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @MethodSource("steps")
    void testRoundTrip(BrewingStep step) {
        NamespacedKey key = new NamespacedKey("test", "round_trip");
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        pdc.set(key, BREWING_STEP, step);
        BrewingStep retrievedStep = pdc.get(key, BREWING_STEP);
        assertEquals(step, retrievedStep);
    }

    private static final List<BrewingStep> STEPS = List.of(
            new MixStepImpl(
                    new PassedMoment(20),
                    Map.of(SimpleIngredient.from("wheat").get(), 1)
            ),
            new CookStepImpl(
                    new PassedMoment(20),
                    Map.of(SimpleIngredient.from("wheat").get(), 1),
                    CauldronType.LAVA
            ),
            new DistillStepImpl(
                    3
            ),
            new AgeStepImpl(
                    new PassedMoment(20),
                    BarrelType.ACACIA
            )
    );

    public static Stream<Arguments> steps() {
        return Stream.of(
                Arguments.of(STEPS.get(0)),
                Arguments.of(new MixStepImpl(
                        new PassedMoment(20),
                        Map.of(SimpleIngredient.from("wheat").get(), 1),
                        CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
                )),
                Arguments.of(STEPS.get(1)),
                Arguments.of(new CookStepImpl(
                        new PassedMoment(20),
                        Map.of(SimpleIngredient.from("wheat").get(), 1),
                        CauldronType.LAVA,
                        CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
                )),
                Arguments.of(STEPS.get(2)),
                Arguments.of(new DistillStepImpl(
                        3,
                        CollectionUtil.sequencedSetOf(UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"))
                )),
                Arguments.of(STEPS.get(3)),
                Arguments.of(new AgeStepImpl(
                        new PassedMoment(20),
                        BarrelType.ACACIA,
                        CollectionUtil.sequencedSetOf(
                                UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                                UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4")
                        )
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("conversionArguments")
    void testV1Conversion(String brewDataFile, BrewingStep expected) throws IOException {
        byte[] bytes = FileUtil.readInternalResourceBytes("/brew/" + brewDataFile);
        BrewingStep step = BREWING_STEP.fromPrimitive(bytes, new PersistentDataContainerMock().getAdapterContext());
        assertEquals(expected, step);
    }

    public static Stream<Arguments> conversionArguments() {
        return Stream.of(
                Arguments.of("mix_v1.dat", STEPS.get(0)),
                Arguments.of("cook_v1.dat", STEPS.get(1)),
                Arguments.of("distill_v1.dat", STEPS.get(2)),
                Arguments.of("age_v1.dat", STEPS.get(3))
        );
    }

    private static void export(BrewingStep step, String fileName) throws IOException {
        byte[] bytes = BREWING_STEP.toPrimitive(step, new PersistentDataContainerMock().getAdapterContext());
        Files.write(Path.of("./src/test/resources/brew/" + fileName), bytes);
        System.out.println(Arrays.toString(bytes));
    }

}
