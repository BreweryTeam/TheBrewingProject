package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class BreweryStructureConfig extends OkaeriConfig {

    @CustomKey("schem_file")
    String schemFileName;

    @CustomKey("meta")
    BreweryStructure.Meta meta;

    @Exclude
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);

    public BreweryStructure toStructure(Path configFilePath, List<StructureMatcher> matchers) {
        Path schemFile = configFilePath.resolveSibling(schemFileName);
        String schemName = SCHEM_PATTERN.matcher(configFilePath.getFileName().toString()).replaceAll("");
        Schematic schematic = new SchematicReader().read(schemFile);
        String matcherName = (String) meta.data().get(StructureMeta.BLOCK_MATCHER);
        Preconditions.checkArgument(matcherName != null,
                "Missing key 'block_matcher' in structure meta: " + configFilePath.getFileName()
        );
        return new BreweryStructure(schematic, schemName, meta, schemName, matchers.stream()
                .filter(matcher -> matcherName.equalsIgnoreCase(matcher.getName()))
                .toList()
        );
    }
}
