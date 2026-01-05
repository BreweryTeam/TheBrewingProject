package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.serialize.Serializer;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public record IngredientMeta<T>(BreweryKey key, Serializer<T> serializer) implements BreweryKeyed {

    public static IngredientMeta<Double> SCORE = new IngredientMeta<>(
            BreweryKey.parse("score"),
            Serializer.compile(String::valueOf, Double::parseDouble, Double.class::isInstance)
    );
    public static IngredientMeta<Component> DISPLAY_NAME_OVERRIDE = new IngredientMeta<>(
            BreweryKey.parse("display_name_override"),
            Serializer.compile(MiniMessage.miniMessage()::serialize, MiniMessage.miniMessage()::deserialize, Component.class::isInstance)
    );
}
