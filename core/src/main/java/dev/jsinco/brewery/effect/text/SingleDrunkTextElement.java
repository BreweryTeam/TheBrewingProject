package dev.jsinco.brewery.effect.text;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record SingleDrunkTextElement(String text, int percentage, int alcohol) implements DrunkTextElement {
    private static final Random RANDOM = new Random();


    @Override
    public @NotNull List<TextTransformation> findTransform(String initial) {
        if (percentage < RANDOM.nextInt(0, 101)) {
            return new ArrayList<>();
        }
        int randomPos = RANDOM.nextInt(0, initial.length() + 1);
        List<TextTransformation> output = new ArrayList<>();
        output.add(new TextTransformation(text, randomPos, randomPos, alcohol()));
        return output;
    }
}
