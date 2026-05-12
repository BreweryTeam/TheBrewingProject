package dev.jsinco.brewery.bukkit.util.color;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.overlay.Overlay;
import team.unnamed.creative.texture.Texture;

record ResourceResolver(ResourcePack resourcePack) {


    public @Nullable Model resolveModel(Key key) {
        for (Overlay overlay : resourcePack.overlays()) {
            Model model = overlay.model(key);
            if (model != null) {
                return model;
            }
        }
        return resourcePack.model(key);
    }

    public @Nullable Texture resolveTexture(Key key) {
        if (!key.value().contains(".")) {
            key = Key.key(key.namespace(), key.value() + ".png");
        }
        for (Overlay overlay : resourcePack.overlays()) {
            Texture texture = overlay.texture(key);
            if (texture != null) {
                return texture;
            }
        }
        return resourcePack.texture(key);
    }
}
