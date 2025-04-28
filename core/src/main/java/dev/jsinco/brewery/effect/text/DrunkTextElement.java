package dev.jsinco.brewery.effect.text;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DrunkTextElement {

    @NotNull
    List<TextTransformation> findTransform(String initial);

    record TextTransformation(String replacement, int from, int to, int alcohol) {
    }
}
