package dev.jsinco.brewery.effect.text;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class DrunkTextRegistry {

    Map<Integer, List<DrunkTextElement>> drunkenTexts = new HashMap<>();


    public void load(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String replace = jsonObject.has("from") ? jsonObject.get("from").getAsString() : null;
                String replacement = jsonObject.get("to").getAsString();
                int percentage = jsonObject.get("percentage").getAsInt();
                int alcohol = jsonObject.has("alcohol") ? jsonObject.get("alcohol").getAsInt() : 0;
                if (alcohol < 0 || alcohol > 100) {
                    Logger.logErr("Alcohol outside range for element: " + jsonObject);
                    continue;
                }
                if (percentage < 0 || percentage > 100) {
                    Logger.logErr("Percentage outside range for element: " + jsonObject);
                    continue;
                }
                DrunkTextElement drunkTextElement = replace != null ?
                        new DrunkTextPattern(Pattern.compile(replace, CASE_INSENSITIVE), replacement, percentage, alcohol) : new SingleDrunkTextElement(replacement, percentage, alcohol);
                for (int i = alcohol; i <= 100; i++) {
                    drunkenTexts.computeIfAbsent(i, ignored -> new ArrayList<>()).add(drunkTextElement);
                }
            }
        }
    }

    public List<DrunkTextElement> getTextTransformers(int alcohol) {
        Preconditions.checkArgument(alcohol >= 0 && alcohol <= 100, "Alcohol outside range");
        return drunkenTexts.computeIfAbsent(alcohol, ignored -> new ArrayList<>());
    }

    public void clear() {
        drunkenTexts.clear();
    }
}
