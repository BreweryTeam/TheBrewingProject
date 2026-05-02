package dev.jsinco.brewery.datagenerator;

import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DataGenerator {

    private static final Map<String, Integer> BIOME_COLORED_BLOCKS = Map.of(
            "short_grass", 0x7cbd6b,
            "_leaves", 0x71a74d,
            "vine", 0x48b518,
            "sugar_cane", 0x8eb971,
            "lily_pad", 0x208030,
            "seagrass", 0x4d9e3f, "kelp", 0x4d9e3f,
            "dry_grass", 0xa89060,
            "dry_bush", 0x946b44, "bush", 0x71a74d
    );

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length != 1) {
            System.out.print("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);
        URL url1 = ClassLoader.getSystemResource("assets/minecraft/textures/item/apple.png");
        URL url2 = ClassLoader.getSystemResource("assets/minecraft/textures/block/acacia_log.png");
        URI uri1 = url1.toURI();
        URI uri2 = url2.toURI();
        try {
            generateColorData(List.of(Paths.get(uri1), Paths.get(uri2)), outputFolder);
        } catch (FileSystemNotFoundException e) {
            try (FileSystem fs = FileSystems.newFileSystem(uri1, Collections.emptyMap())) {
                generateColorData(List.of(Paths.get(uri1), Paths.get(uri2)), outputFolder);
            }
        }
    }

    private static void generateColorData(List<Path> paths, File outputFolder) throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (Path path : paths) {
            Path directory = path.getParent();
            try (Stream<Path> walk = Files.walk(directory)) {
                Iterator<Path> walkIterator = walk.iterator();
                while (walkIterator.hasNext()) {
                    Path next = walkIterator.next();
                    if (!next.toString().endsWith(".png")) {
                        continue;
                    }
                    String name = next.getFileName().toString().replace(".png", "");
                    if (jsonObject.has(name)) {
                        continue;
                    }
                    try (InputStream inputStream = Files.newInputStream(next)) {
                        BufferedImage image = ImageIO.read(inputStream);
                        Color color = replaceBiomeColors(ColorUtil.getAverageColor(image), name);
                        jsonObject.addProperty(name, Integer.toHexString(color.getRGB() & 0x00ffffff));
                    }
                }
            }
        }
        JsonUtil.dump(jsonObject, new File(outputFolder, "colors.json"));
    }

    private static Color replaceBiomeColors(Color initial, String name) {
        for (String edgeCase : BIOME_COLORED_BLOCKS.keySet()) {
            if (name.contains(edgeCase)) {
                return new Color(BIOME_COLORED_BLOCKS.get(edgeCase));
            }
        }
        return initial;
    }
}
