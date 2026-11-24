package dev.jsinco.brewery.api.util.color;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

@ApiStatus.Internal
@NullMarked
public interface ResourcePackColors {

    @Nullable Color modelColor(Key modelKey);

    void addSource(ResourcePackSource source);


    @Nullable Color customModelDataColor(Key key, int customModelData);
}
