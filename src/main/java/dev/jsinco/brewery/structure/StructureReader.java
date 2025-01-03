package dev.jsinco.brewery.structure;

import com.google.gson.*;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import dev.thorinwasher.schem.blockpalette.BlockPaletteParser;
import org.bukkit.*;
import org.joml.Vector3i;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureReader {
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_PATTERN = Pattern.compile("^#");

    public static Map<String, BreweryStructure> fromJson(Path path) throws IOException, StructureReadException {
        try (Reader reader = new InputStreamReader(new BufferedInputStream(Files.newInputStream(path)))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String schemFileName = jsonObject.get("schemFile").getAsString();
            Optional<Vector3i> origin = Optional.ofNullable(jsonObject.get("origin"))
                    .map(JsonElement::getAsJsonArray)
                    .map(StructureReader::toVector3i);
            Path schemFile = path.resolveSibling(schemFileName);
            String schemName = SCHEM_PATTERN.matcher(path.getFileName().toString()).replaceAll("");
            return loadStructures(jsonObject.get("materialReplacements").getAsJsonObject(), schemFile, origin, schemName);
        }
    }

    private static Map<String, BreweryStructure> loadStructures(JsonObject materialReplacements, Path schemFile, Optional<Vector3i> origin, String schemName) throws StructureReadException {
        JsonElement excludedPatternJson = materialReplacements.get("excludedPattern");
        Pattern excludedPattern = null;
        if (excludedPatternJson instanceof JsonPrimitive) {
            excludedPattern = Pattern.compile(excludedPatternJson.getAsString());
        } else if (excludedPatternJson != null) {
            throw new StructureReadException("Expected json primitive string");
        }
        Set<String> includedMaterialSubstitutionsPatterns = new HashSet<>();
        for (Material taggedMaterial : parseMaterials(materialReplacements.get("materials").getAsString())) {
            if (excludedPattern != null) {
                includedMaterialSubstitutionsPatterns.add(excludedPattern.matcher(taggedMaterial.getKey().getKey()).replaceAll(""));
                continue;
            }
            includedMaterialSubstitutionsPatterns.add(taggedMaterial.getKey().getKey());
        }
        Map<String, BreweryStructure> output = new HashMap<>();
        for (String materialSubstitutionPattern : includedMaterialSubstitutionsPatterns) {
            BlockPaletteParser blockPaletteParser = new SubtitutedBlockPaletteParser(includedMaterialSubstitutionsPatterns, materialSubstitutionPattern);
            Schematic schem = new SchematicReader().withBlockPaletteParser(blockPaletteParser).read(schemFile);
            BreweryStructure struct = origin.map(vector3i -> new BreweryStructure(schem, vector3i)).orElse(new BreweryStructure(schem));
            output.put(schemName + "$" + materialSubstitutionPattern, struct);
        }
        return output;
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
