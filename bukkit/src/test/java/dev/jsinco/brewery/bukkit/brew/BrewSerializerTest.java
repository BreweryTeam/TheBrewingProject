package dev.jsinco.brewery.bukkit.brew;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import dev.jsinco.brewery.util.CollectionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BrewSerializerTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(1);

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
        MockBukkit.load(TheBrewingProject.class);
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void deserialize() {
        String jsonStr = """
                [{"type":"cook","brew_time":20,"cauldron_type":"brewery:lava","ingredients":{"minecraft:wheat":1}},{"type":"distill","runs":3},{"type":"age","age":20,"barrel_type":"brewery:acacia"}]""";
        JsonArray json = JsonParser.parseString(jsonStr).getAsJsonArray();

        CompletableFuture<Brew> future = BrewImpl.SERIALIZER.deserialize(json, BukkitIngredientManager.INSTANCE);
        Brew deserialized = assertTimeout(TIMEOUT, () -> future.get());
        assertEquals(sampleBrew(), deserialized);
    }

    @ParameterizedTest
    @MethodSource("brews")
    void roundTrip(Brew brew) {
        JsonArray serialized = BrewImpl.SERIALIZER.serialize(brew);

        CompletableFuture<Brew> future = BrewImpl.SERIALIZER.deserialize(serialized, BukkitIngredientManager.INSTANCE);
        Brew deserialized = assertTimeout(TIMEOUT, () -> future.get());
        assertEquals(brew, deserialized);
    }

    private static Stream<Arguments> brews() {
        return Stream.of(
                Arguments.of(new BrewImpl(
                        List.of()
                )),
                Arguments.of(sampleBrew()),
                Arguments.of(new BrewImpl(
                        List.of(
                                new CookStepImpl(
                                        new PassedMoment(20),
                                        Map.of(SimpleIngredient.from("wheat").get(), 1),
                                        CauldronType.LAVA,
                                        CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
                                ),
                                new DistillStepImpl(
                                        3,
                                        CollectionUtil.sequencedSetOf(UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"))
                                ),
                                new AgeStepImpl(
                                        new PassedMoment(20),
                                        BarrelType.ACACIA,
                                        CollectionUtil.sequencedSetOf(
                                                UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                                                UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919")
                                        )
                                )
                        )
                ))
        );
    }

    private static BrewImpl sampleBrew() {
        return new BrewImpl(
                List.of(
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
                )
        );
    }

}
