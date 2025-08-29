package dev.jsinco.brewery.api.effect.modifier;

import net.kyori.adventure.text.Component;

public record ModifierDisplay(Component displayName, ModifierExpression expression, DisplayType type,
                              DisplayWindow displayWindow) {

    public enum DisplayType {
        SKULLS,
        BARS,
        STARS
    }

    public enum DisplayWindow {
        CHAT,
        BAR,
        TITLE
    }
}
