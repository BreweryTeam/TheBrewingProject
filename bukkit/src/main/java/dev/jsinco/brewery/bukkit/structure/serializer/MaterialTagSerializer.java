package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.structure.MaterialTag;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.structure.StructureReadException;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaterialTagSerializer implements ObjectSerializer<MaterialTag> {

    private static final Pattern TAG_PATTERN = Pattern.compile("^#");

    @Override
    public boolean supports(@NonNull Class<? super MaterialTag> type) {
        return MaterialTag.class == type;
    }

    @Override
    public void serialize(@NonNull MaterialTag object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.addCollection("materials", object.materials(), Holder.Material.class);
        if (object.xRegion() > 1) {
            data.add("x_region", object.xRegion());
        }
        if (object.yRegion() > 1) {
            data.add("y_region", object.yRegion());
        }
        if (object.zRegion() > 1) {
            data.add("z_region", object.zRegion());
        }
    }

    @Override
    public MaterialTag deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Set<Holder.Material> materials = data.getAsSet("materials", Holder.Material.class);
        Integer xRegion = data.get("x_region", Integer.class);
        Integer yRegion = data.get("y_region", Integer.class);
        Integer zRegion = data.get("z_region", Integer.class);
        return new MaterialTag(
                materials,
                xRegion == null ? 1 : xRegion,
                yRegion == null ? 1 : yRegion,
                zRegion == null ? 1 : zRegion
        );
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
}
