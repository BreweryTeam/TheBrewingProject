package dev.jsinco.brewery.bukkit.util.color;

import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.model.ModelTexture;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader;
import team.unnamed.creative.texture.Texture;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackColors {

    private final Map<Key, Color> colors = new ConcurrentHashMap<>();

    public void init() {
        Bukkit.getAsyncScheduler().runNow(TheBrewingProject.getInstance(), ignored -> {
            org.bukkit.packs.ResourcePack bukkitPack = Bukkit.getServerResourcePack();
            if (bukkitPack == null) {
                return;
            }
            ResourcePack resourcePack = MinecraftResourcePackReader.minecraft().readFromZipFile(
                    Path.of(URI.create(bukkitPack.getUrl()))
            );
            for (Model modelTextures : resourcePack.models()) {
                List<BufferedImage> layers = modelTextures.textures().layers()
                        .stream()
                        .map(modelTexture -> readImage(resourcePack, modelTexture))
                        .toList();
                BufferedImage output = null;
                for (BufferedImage layer : layers) {
                    if (output == null) {
                        output = layer;
                        continue;
                    }
                    output = merge(output, layer);
                }
                colors.put(modelTextures.key(), ColorUtil.getDistinctColor(output));
            }
        });
    }

    public @Nullable Color modelColor(Key modelKey) {
        return colors.get(modelKey);
    }

    private static BufferedImage merge(BufferedImage first, BufferedImage override) {
        int height = Math.max(first.getHeight(), override.getHeight());
        int width = Math.max(first.getWidth(), override.getWidth());
        BufferedImage output = new BufferedImage(width, height, first.getType());
        writeTo(output, first);
        writeTo(output, override);
        return output;
    }

    private static void writeTo(BufferedImage to, BufferedImage content) {
        for (int x = 0; x < content.getWidth(); x++) {
            for (int y = 0; y < content.getHeight(); y++) {
                to.setRGB(x, y, content.getRGB(x, y));
            }
        }
    }

    private static @Nullable BufferedImage readImage(ResourcePack resourcePack, ModelTexture modelTexture) {
        Key key = modelTexture.key();
        if (key == null) {
            return null;
        }
        Texture texture = resourcePack.texture(key);
        if (texture == null) {
            return null;
        }
        try (InputStream inputStream = new ByteArrayInputStream(texture.data().toByteArray())) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            Logger.logAndTrackErr(e);
        }
        return null;
    }
}
