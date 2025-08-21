package dev.jsinco.brewery.bukkit.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.util.Util;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import org.bukkit.*;
import org.joml.Vector3i;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StructureReader {
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_PATTERN = Pattern.compile("^#");
    private static final Pattern SCHEM_FILE_PATTERN = Pattern.compile("[a-zA-Z_0-9\\-]+");

    public static BreweryStructure fromInternalResourceJson(String string) throws IOException, StructureReadException {
        URL url = Util.class.getResource(string);
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new StructureReadException(e);
        }
        try {
            return fromJson(Paths.get(uri));
        } catch (FileSystemNotFoundException e) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
                return fromJson(fileSystem.getPath(uri.toString().split("!")[1]));
            }
        }
    }

    public static BreweryStructure fromJson(Path path) throws IOException, StructureReadException {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String schemFileName = jsonObject.get("schem_file").getAsString();
            Optional<Vector3i> origin = Optional.ofNullable(jsonObject.get("origin"))
                    .map(JsonElement::getAsJsonArray)
                    .map(StructureReader::toVector3i);
            Path schemFile = path.resolveSibling(schemFileName);
            String schemName = SCHEM_PATTERN.matcher(path.getFileName().toString()).replaceAll("");
            Schematic schematic = new SchematicReader().read(schemFile);
            Map<StructureMeta<?>, Object> structureMeta = jsonObject.get("meta").getAsJsonObject()
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        StructureMeta<?> meta = dev.jsinco.brewery.util.Registry.STRUCTURE_META.get(BreweryKey.parse(entry.getKey()));
                        if (meta == null) {
                            throw new StructureReadException("Unknown meta: " + entry.getKey());
                        }
                        Object value = meta.deserializer().apply(entry.getValue());
                        return new Pair<>(meta, value);
                    })
                    .collect(Collectors.toMap(Pair::first, Pair::second));
            return origin.map(vector3i -> new BreweryStructure(schematic, List.of(vector3i), schemName, structureMeta)).orElse(new BreweryStructure(schematic, schemName, structureMeta));
        }
    }

    private static Set<Material> parseMaterials(String materialString) throws StructureReadException {
        String[] split = materialString.split(",");
        Set<Material> output = new HashSet<>();
        for (String arg : split) {
            Matcher tagMatcher = TAG_PATTERN.matcher(arg);
            if (tagMatcher.find()) {
                String tagName = tagMatcher.replaceAll("").toLowerCase(Locale.ROOT);
                Tag<Material> materialsTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagName), Material.class);
                if (materialsTag == null) {
                    throw new StructureReadException("Unknown material tag: " + NamespacedKey.minecraft(tagName));
                }
                output.addAll(materialsTag.getValues());
                continue;
            }
            Material material = Registry.MATERIAL.get(NamespacedKey.minecraft(arg.toLowerCase(Locale.ROOT)));
            if (material == null) {
                throw new StructureReadException("Unknown material: " + NamespacedKey.minecraft(arg.toLowerCase(Locale.ROOT)));
            }
            output.add(material);
        }
        return output;
    }

    private static Vector3i toVector3i(JsonArray jsonArray) {
        return new Vector3i(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsInt(), jsonArray.get(2).getAsInt());
    }
}
