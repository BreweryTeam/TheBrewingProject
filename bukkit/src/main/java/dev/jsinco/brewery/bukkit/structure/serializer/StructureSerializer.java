package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.bukkit.structure.BreweryStructure;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.joml.Vector3i;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class StructureSerializer implements ObjectSerializer<BreweryStructure> {

    private final Path path;
    private static final Pattern SCHEM_PATTERN = Pattern.compile("\\.json", Pattern.CASE_INSENSITIVE);

    public StructureSerializer(Path filePath) {
        this.path = filePath;
    }

    @Override
    public boolean supports(@NonNull Class<? super BreweryStructure> type) {
        return BreweryStructure.class == type;
    }

    @Override
    public void serialize(@NonNull BreweryStructure object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("schem_file", object.getSchemFileName());
        BreweryStructure.EntryPoints entryPoints = object.getEntryPoints();
        if (entryPoints.customDefinition()) {
            if (entryPoints.entryPoints().size() == 1) {
                data.add("entry_point", entryPoints.entryPoints().getFirst());
            } else {
                data.addCollection("entry_points", entryPoints.entryPoints(), Vector3i.class);
            }
        }
        data.add("meta", object.getMeta());
    }

    @Override
    public BreweryStructure deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String schemFileName = data.get("schem_file", String.class);
        Optional<List<Vector3i>> origin = Optional.ofNullable(data.get("origin", Vector3i.class))
                .or(() -> Optional.ofNullable(data.get("entry_point", Vector3i.class)))
                .map(List::of)
                .or(() -> Optional.ofNullable(data.getAsList("entry_points", Vector3i.class)));
        Path schemFile = path.resolveSibling(schemFileName);
        String schemName = SCHEM_PATTERN.matcher(path.getFileName().toString()).replaceAll("");
        Schematic schematic = new SchematicReader().read(schemFile);
        BreweryStructure.Meta structureMeta = data.get("meta", BreweryStructure.Meta.class);
        return origin.map(vector3is ->
                        new BreweryStructure(schematic, new BreweryStructure.EntryPoints(vector3is, true), schemName, structureMeta, schemName))
                .orElse(new BreweryStructure(schematic, schemName, structureMeta, schemName));
    }
}
