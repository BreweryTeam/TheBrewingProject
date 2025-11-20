package dev.jsinco.brewery.bukkit.structure;

import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

public class BreweryStructureConfig extends OkaeriConfig {

    @CustomKey("schem_file")
    String schemFileName;

    @CustomKey("meta")
    BreweryStructure.Meta meta;

    @Exclude
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);

    public BreweryStructure toStructure(Path configFilePath) {
        Path schemFile = configFilePath.resolveSibling(schemFileName);
        String schemName = SCHEM_PATTERN.matcher(configFilePath.getFileName().toString()).replaceAll("");
        Schematic schematic = new SchematicReader().read(schemFile);
        return new BreweryStructure(schematic, schemName, meta, schemName);
    }
}
