package dev.jsinco.brewery.effect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.effect.text.DrunkTextPattern;
import dev.jsinco.brewery.effect.text.DrunkTextTransformer;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrunkTextPatternTest {


    @ParameterizedTest
    @MethodSource("getDrunkTextTransformations")
    void findTransform(JsonObject data) {
        Pattern from = Pattern.compile(data.get("from").getAsString(), Pattern.CASE_INSENSITIVE);
        String to = data.get("to").getAsString();
        String text = data.get("text").getAsString();
        String expected = data.get("expected").getAsString();
        DrunkTextPattern drunkTextPattern = new DrunkTextPattern(from, to, 100, new DrunkenModifier(
                "alcohol",
                ModifierExpression.ZERO,
                ModifierExpression.ZERO,
                0D, 100D,
                Component.text("Alcohol")), 0D);
        assertEquals(expected, DrunkTextTransformer.transform(text, List.of(drunkTextPattern)));
    }

    public static Stream<Arguments> getDrunkTextTransformations() throws IOException {
        try (InputStream inputStream = DrunkTextPattern.class.getResourceAsStream("/valid_drunk_text.json")) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(inputStreamReader)
                        .getAsJsonArray()
                        .asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(Arguments::of);
            }
        }
    }

}