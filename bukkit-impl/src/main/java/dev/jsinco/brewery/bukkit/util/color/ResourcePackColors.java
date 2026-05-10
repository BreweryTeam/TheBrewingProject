package dev.jsinco.brewery.bukkit.util.color;

import dev.jsinco.brewery.api.util.Logger;
import io.netty.handler.codec.http.HttpHeaderNames;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.item.CompositeItemModel;
import team.unnamed.creative.item.ConditionItemModel;
import team.unnamed.creative.item.EmptyItemModel;
import team.unnamed.creative.item.Item;
import team.unnamed.creative.item.ItemModel;
import team.unnamed.creative.item.RangeDispatchItemModel;
import team.unnamed.creative.item.ReferenceItemModel;
import team.unnamed.creative.item.SelectItemModel;
import team.unnamed.creative.item.SpecialItemModel;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.model.ModelTexture;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader;
import team.unnamed.creative.texture.Texture;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackColors {

    private final Map<Key, Color> itemModelColors = new ConcurrentHashMap<>();
    private final Map<Key, Map<Float, Color>> customModelDataColors = new ConcurrentHashMap<>();
    private List<ResourcePackSource> sources = new ArrayList<>();

    public void init() {
        List<ResourcePack> resourcePacks;
        try {
            resourcePacks = readResourcePacks();
        } catch (IOException | InterruptedException e) {
            Logger.logAndTrackErr(e);
            return;
        }
        if (resourcePacks.isEmpty()) {
            Logger.log("Resource packs was empty");
            return;
        }
        for (ResourcePack resourcePack : resourcePacks) {
            readResourcePackContent(resourcePack);
        }
    }

    private void readResourcePackContent(ResourcePack resourcePack) {
        if (resourcePack.items().isEmpty()) {
            Logger.log("Items was empty");
        }
        for (Item item : resourcePack.items()) {
            ItemModel itemModel = item.model();
            if (itemModel instanceof RangeDispatchItemModel rangeDispatchItemModel) {
                Map<Float, Color> colors = new HashMap<>();
                for (RangeDispatchItemModel.Entry entry : rangeDispatchItemModel.entries()) {
                    BufferedImage modelImage = readItemModel(entry.model(), resourcePack);
                    if (modelImage == null) {
                        Logger.log("Model image was null for item: " + item.key());
                        Logger.log("Custom model data: " + entry.threshold());
                        continue;
                    }
                    colors.put(entry.threshold(), ColorUtil.getDistinctColor(modelImage));
                }
                customModelDataColors.put(item.key(), colors);
                Logger.log("Added custom model data: " + item.key() + colors);
                continue;
            }
            BufferedImage modelImage = readItemModel(item.model(), resourcePack);
            if (modelImage == null) {
                Logger.log("Model image was null for item: " + item.key());
                continue;
            }
            itemModelColors.put(item.key(), ColorUtil.getDistinctColor(modelImage));
            Logger.log("Added custom item model: " + item.key());
        }
    }

    private List<ResourcePack> readResourcePacks() throws IOException, InterruptedException {
        if (sources.isEmpty()) {
            org.bukkit.packs.ResourcePack bukkitPack = Bukkit.getServerResourcePack();
            if (bukkitPack == null) {
                Logger.log("Server resource pack was null");
                return List.of();
            }
            sources.add(new HttpResourcePackSource(bukkitPack.getUrl(), false));
        }
        MinecraftResourcePackReader reader = MinecraftResourcePackReader.builder()
                .lenient(true)
                .build();
        List<ResourcePack> output = new ArrayList<>();
        for (ResourcePackSource source : List.copyOf(sources)) {
            output.add(source.readPack());
        }
        return output;
    }

    private @Nullable BufferedImage readItemModel(ItemModel itemModel, ResourcePack resourcePack) {
        return switch (itemModel) {
            case CompositeItemModel compositeItemModel -> readCompositeModel(compositeItemModel, resourcePack);
            case SpecialItemModel specialItemModel ->
                    readModel(resourcePack.model(specialItemModel.base()), resourcePack);
            case RangeDispatchItemModel rangeDispatchItemModel ->
                    readItemModel(rangeDispatchItemModel.fallback(), resourcePack);
            case ReferenceItemModel referenceItemModel ->
                    readModel(resourcePack.model(referenceItemModel.model()), resourcePack);
            case ConditionItemModel conditionItemModel -> readItemModel(conditionItemModel.onFalse(), resourcePack);
            case SelectItemModel selectItemModel -> readItemModel(
                    selectItemModel.fallback(), resourcePack
            );
            case EmptyItemModel _ -> null;
            case null -> {
                Logger.log("Model was null");
                yield null;
            }
            default -> {
                Logger.log("Unknown item model type: " + itemModel.getClass().getName());
                yield null;
            }
        };
    }

    private BufferedImage readModel(Model model, ResourcePack resourcePack) {
        if (model == null) {
            Logger.log("Model was null");
            return null;
        }
        List<BufferedImage> layers = model.textures().layers().stream()
                .map(modelTexture -> readImage(resourcePack, modelTexture))
                .toList();
        if (layers.isEmpty()) {
            return mergeImages(model.textures().variables().entrySet().stream()
                    .filter(entry -> entry.getKey().matches("\\d+"))
                    .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey())))
                    .map(Map.Entry::getValue)
                    .map(modelTexture -> readImage(resourcePack, modelTexture))
                    .toList()
            );
        }
        return mergeImages(layers);
    }

    private @Nullable BufferedImage readCompositeModel(CompositeItemModel compositeItemModel, ResourcePack resourcePack) {
        List<BufferedImage> layers = compositeItemModel.models()
                .stream()
                .map(modelTexture -> readItemModel(modelTexture, resourcePack))
                .toList();
        if (layers.isEmpty()) {
            Logger.log("Image layers was empty");
        }
        return mergeImages(layers);
    }

    private @Nullable BufferedImage mergeImages(List<BufferedImage> images) {
        BufferedImage output = null;
        for (BufferedImage layer : images) {
            if (output == null) {
                output = layer;
                continue;
            }
            output = merge(output, layer);
        }
        return output;
    }

    public @Nullable Color modelColor(Key modelKey) {
        return itemModelColors.get(modelKey);
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

    private static @Nullable BufferedImage readImage(ResourcePack resourcePack, ModelTexture model) {
        Key key = model.key();
        if (key == null) {
            Logger.log("Model key was null");
            return null;
        }
        if (!key.value().contains(".")) {
            key = Key.key(key.namespace(), key.value() + ".png");
        }
        Texture texture = resourcePack.texture(key);
        if (texture == null) {
            Logger.log("Texture was null: " + key);
            return null;
        }
        try (InputStream inputStream = new ByteArrayInputStream(texture.data().toByteArray())) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            Logger.logAndTrackErr(e);
        }
        return null;
    }

    public void addSource(ResourcePackSource source) {
        this.sources.add(source);
    }

    public @Nullable Color customModelDataColor(@NotNull Key key, int customModelData) {
        return customModelDataColors.getOrDefault(key, Map.of())
                .get((float) customModelData);
    }

    public interface ResourcePackSource {

        MinecraftResourcePackReader READER = MinecraftResourcePackReader.builder()
                .lenient(true)
                .build();

        ResourcePack readPack() throws IOException, InterruptedException;
    }

    public record HttpResourcePackSource(String url, boolean sha256) implements ResourcePackSource {

        public static HttpResourcePackSource withSha256(String url) {
            return new HttpResourcePackSource(url, true);
        }

        public static HttpResourcePackSource withoutSha256(String url) {
            return new HttpResourcePackSource(url, false);
        }

        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            URI uri = URI.create(url);
            Logger.log("Sending http get request: " + uri);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .headers(
                            HttpHeaderNames.USER_AGENT.toString(), "Minecraft Java/1.21.11",
                            "X-Minecraft-Version", "1.21.11"
                    ).GET()
                    .build();
            try (HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()) {
                if (sha256) {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    return READER.readFromInputStream(new ByteArrayInputStream(
                            messageDigest.digest(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                                    .body())
                    ));
                } else {
                    return READER.readFromInputStream(new ByteArrayInputStream(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                            .body()
                    ));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }


    public record FileResourcePackSource(File file) implements ResourcePackSource {

        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            if (file.isDirectory()) {
                return READER.readFromDirectory(file);
            } else {
                return READER.readFromZipFile(file);
            }
        }
    }
}
