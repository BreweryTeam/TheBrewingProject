package dev.jsinco.brewery.bukkit.util.color;

import dev.jsinco.brewery.api.util.Logger;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackColors {

    private final Map<Key, Color> itemModelColors = new ConcurrentHashMap<>();
    private final Map<Key, Map<Float, Color>> customModelDataColors = new ConcurrentHashMap<>();
    private String url = null;

    public void init() {
        String packUrl;
        if (url == null) {
            org.bukkit.packs.ResourcePack bukkitPack = Bukkit.getServerResourcePack();
            if (bukkitPack == null) {
                Logger.log("Server resource pack was null");
                return;
            }
            packUrl = bukkitPack.getUrl();
        } else {
            packUrl = url;
        }
        ResourcePack resourcePack;
        try (InputStream inputStream = URI.create(packUrl).toURL().openStream()) {
            resourcePack = MinecraftResourcePackReader.builder()
                    .lenient(true)
                    .build()
                    .readFromInputStream(inputStream);
        } catch (IOException e) {
            Logger.logAndTrackErr(e);
            return;
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
                continue;
            }
            BufferedImage modelImage = readItemModel(item.model(), resourcePack);
            if (modelImage == null) {
                Logger.log("Model image was null for item: " + item.key());
                continue;
            }
            itemModelColors.put(item.key(), ColorUtil.getDistinctColor(modelImage));
        }
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

    public void setUrl(String url) {
        this.url = url;
    }

    public @Nullable Color customModelDataColor(@NotNull Key key, int customModelData) {
        return customModelDataColors.getOrDefault(key, Map.of())
                .get((float) customModelData);
    }
}
