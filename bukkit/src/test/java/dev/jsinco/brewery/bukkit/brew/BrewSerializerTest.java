package dev.jsinco.brewery.bukkit.brew;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class BrewSerializerTest {

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
        MockBukkit.load(TheBrewingProject.class);
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @MethodSource("brews")
    void roundTrip(Brew brew) {
        JsonElement serialized = BrewImpl.SERIALIZER.serialize(brew, BukkitIngredientManager.INSTANCE);

        CompletableFuture<Brew> future = BrewImpl.SERIALIZER.deserialize(serialized, BukkitIngredientManager.INSTANCE);
        Brew deserialized = assertTimeout(Duration.ofSeconds(5), () -> future.get());
        assertEquals(brew, deserialized);
    }

    private static Stream<Arguments> brews() {
        return Stream.of(
                Arguments.of(new BrewImpl(
                        List.of()
                )),
                Arguments.of(sampleBrew()),
                Arguments.of(sampleBrew()
                        .withMeta(Key.key("test", "sample"), MetaDataType.STRING, "sample text")
                )
        );
    }

    @Test
    void version0Conversion() {
        String jsonStr = """
                [{"type":"cook","brew_time":20,"cauldron_type":"brewery:lava","ingredients":{"minecraft:wheat":1}},{"type":"distill","runs":3},{"type":"age","age":20,"barrel_type":"brewery:acacia"}]""";
        JsonElement json = JsonParser.parseString(jsonStr);

        CompletableFuture<Brew> future = BrewImpl.SERIALIZER.deserialize(json, BukkitIngredientManager.INSTANCE);
        Brew deserialized = assertTimeout(Duration.ofSeconds(5), () -> future.get());
        assertEquals(sampleBrew(), deserialized);
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
