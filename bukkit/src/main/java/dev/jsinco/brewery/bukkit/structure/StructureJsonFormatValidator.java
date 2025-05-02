package dev.jsinco.brewery.bukkit.structure;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import dev.jsinco.brewery.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StructureJsonFormatValidator {

    private StructureJsonFormatValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean validate(Path jsonPath) {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(Files.newInputStream(jsonPath)))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (!(jsonObject.get("meta") instanceof JsonObject metaJson)) {
                return false;
            }
            String fileName = jsonPath.getFileName().toString();
            Map<StructureMeta<?>, Object> structureMeta = metaJson.entrySet()
                    .stream()
                    .map(entry -> {
                        StructureMeta<?> meta = Registry.STRUCTURE_META.get(BreweryKey.parse(entry.getKey()));
                        if (meta == null) {
                            Logging.warning("Unknown meta key in structure '" + fileName + "': " + entry.getKey());
                            return null;
                        }
                        return new Pair<>(meta, meta.deserializer().apply(entry.getValue()));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Pair::first, Pair::second));
            StructureType structureType = get(structureMeta, StructureMeta.TYPE);
            if (structureType == null) {
                Logging.warning("Missing meta key in structure '" + fileName + "': type");
                return false;
            }
            for (StructureMeta<?> meta : structureType.mandatoryMeta()) {
                structureMeta.computeIfAbsent(meta, ignored -> meta.defaultValue());
            }
            JsonObject reformatedJson = new JsonObject();
            for (Map.Entry<StructureMeta<?>, Object> entry : structureMeta.entrySet()) {
                Object value = entry.getValue();
                if (!entry.getKey().validator().test(value)) {
                    Logging.warning("Invalid value for meta type in structure '" + fileName + "':" + entry.getKey().key().key());
                    value = entry.getKey().defaultValue();
                }
                if (entry.getKey().equals(StructureMeta.TYPE)) {
                    reformatedJson.add("type", new JsonPrimitive(((BreweryKeyed) entry.getValue()).key().key()));
                    continue;
                }
                if (Arrays.stream(structureType.mandatoryMeta()).noneMatch(entry.getKey()::equals)) {
                    Logging.warning("Illegal meta in structure '" + fileName + "':" + entry.getKey().key().key());
                    continue;
                }
                switch (value) {
                    case Number number -> reformatedJson.add(entry.getKey().key().key(), new JsonPrimitive(number));
                    case Boolean bool -> reformatedJson.add(entry.getKey().key().key(), new JsonPrimitive(bool));
                    case BreweryKeyed breweryKeyed ->
                            reformatedJson.add(entry.getKey().key().key(), new JsonPrimitive(breweryKeyed.key().key()));
                    case String string -> reformatedJson.add(entry.getKey().key().key(), new JsonPrimitive(string));
                    case null, default ->
                            throw new IllegalStateException("Input should already have been validated, unreachable code.");
                }
            }
            jsonObject.add("meta", reformatedJson);
            dump(jsonObject, jsonPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static <T> T get(Map<StructureMeta<?>, Object> structureMeta, StructureMeta<T> meta) {
        return (T) structureMeta.get(meta);
    }

    public static void dump(JsonElement json, Path destinationFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destinationFile.toFile(), false), StandardCharsets.UTF_8))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("  ");
            gson.toJson(json, jsonWriter);
            writer.print("\n");
        }
    }
}
