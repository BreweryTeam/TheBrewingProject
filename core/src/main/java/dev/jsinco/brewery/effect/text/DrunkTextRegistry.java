package dev.jsinco.brewery.effect.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class DrunkTextRegistry {

    List<DrunkTextElement> drunkenTexts = new ArrayList<>();


    public void load(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String replace = jsonObject.has("from") ? jsonObject.get("from").getAsString() : null;
                String replacement = jsonObject.get("to").getAsString();
                int percentage = jsonObject.get("percentage").getAsInt();
                Optional<DrunkenModifier> optionalModifier = DrunkenModifierSection.modifiers()
                        .drunkenModifiers()
                        .stream()
                        .filter(modifier -> jsonObject.has(modifier.name()))
                        .findAny();
                if (percentage < 0 || percentage > 100) {
                    Logger.logErr("Percentage outside range for element: " + jsonObject);
                    continue;
                }
                double modifierValue = optionalModifier.map(DrunkenModifier::name)
                        .map(jsonObject::get)
                        .map(JsonElement::getAsDouble)
                        .orElse(0D);
                DrunkTextElement drunkTextElement = replace != null ?
                        new DrunkTextPattern(Pattern.compile(replace, CASE_INSENSITIVE), replacement, percentage, optionalModifier.orElse(null), modifierValue) :
                        new SingleDrunkTextElement(replacement, percentage, optionalModifier.orElse(null), modifierValue);
                drunkenTexts.add(drunkTextElement);
            }
        }
    }

    public List<DrunkTextElement> getTextTransformers(Map<DrunkenModifier, Double> alcohol) {
        return drunkenTexts.stream()
                .filter(drunkTextElement -> alcohol.get(drunkTextElement.modifier()) < drunkTextElement.minValue())
                .toList();
    }

    public void clear() {
        drunkenTexts.clear();
    }
}
