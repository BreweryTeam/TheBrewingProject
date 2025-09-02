package dev.jsinco.brewery.api.effect.modifier;

public record ModifierDisplay(String message, ModifierExpression filter, ModifierExpression value,
                              DisplayWindow displayWindow) {

    public enum DisplayWindow {
        CHAT,
        ACTION_BAR,
        TITLE
    }
}
