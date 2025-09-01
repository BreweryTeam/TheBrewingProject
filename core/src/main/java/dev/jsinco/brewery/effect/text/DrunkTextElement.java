package dev.jsinco.brewery.effect.text;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DrunkTextElement {

    @NotNull
    List<TextTransformation> findTransform(String initial);

    DrunkenModifier modifier();

    double minValue();

    record TextTransformation(String replacement, int from, int to) {
    }
}
