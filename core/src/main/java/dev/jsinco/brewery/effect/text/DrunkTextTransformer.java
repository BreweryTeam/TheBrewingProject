package dev.jsinco.brewery.effect.text;

import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DrunkTextTransformer {

    public static String transform(String text, DrunkTextRegistry registry, DrunkStateImpl drunkState) {
        if (!EventSection.events().blurredSpeech() || drunkState.additionalModifierData().isEmpty()) {
            return text;
        }
        return transform(text, registry.getTextTransformers(drunkState.modifiers()));
    }


    public static String transform(String text, List<DrunkTextElement> drunkTextTransformers) {
        List<DrunkTextElement.TextTransformation> transformations = new ArrayList<>();
        for (DrunkTextElement transformer : drunkTextTransformers) {
            transformations.addAll(transformer.findTransform(text));
        }
        transformations.sort(Comparator.comparing(DrunkTextElement.TextTransformation::from));
        List<DrunkTextElement.TextTransformation> clashingTextTransforms = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        int lastIndex = 0;
        for (int i = 0; i < transformations.size(); i++) {
            if (!clashingTextTransforms.isEmpty() && !clashesWithTransforms(transformations.get(i), clashingTextTransforms)) {
                clashingTextTransforms.sort(Comparator.comparing(DrunkTextElement.TextTransformation::alcohol));
                DrunkTextElement.TextTransformation chosenTransform = clashingTextTransforms.getLast();
                output.append(text, lastIndex, chosenTransform.from())
                        .append(chosenTransform.replacement());
                lastIndex = chosenTransform.to();
                clashingTextTransforms.clear();
            }
            clashingTextTransforms.add(transformations.get(i));
        }
        clashingTextTransforms.sort(Comparator.comparing(DrunkTextElement.TextTransformation::alcohol));
        if (!clashingTextTransforms.isEmpty()) {
            DrunkTextElement.TextTransformation chosenTransform = clashingTextTransforms.getLast();
            output.append(text, lastIndex, chosenTransform.from())
                    .append(chosenTransform.replacement())
                    .append(text, chosenTransform.to(), text.length());
        } else {
            output.append(text, lastIndex, text.length());
        }
        return output.toString();
    }

    private static boolean clashesWithTransforms(DrunkTextElement.TextTransformation textTransformation, List<DrunkTextElement.TextTransformation> clashingTextTransforms) {
        int minima = clashingTextTransforms.getFirst().from();
        int maxima = clashingTextTransforms.getFirst().to();
        for (DrunkTextElement.TextTransformation transform : clashingTextTransforms) {
            if (minima > transform.from()) {
                minima = transform.from();
            }
            if (maxima < transform.to()) {
                maxima = transform.to();
            }
        }
        return inRange(textTransformation.to(), maxima, minima) || inRange(textTransformation.from(), maxima, minima);
    }

    private static boolean inRange(int i, int maxima, int minima) {
        return maxima > i && minima <= i;
    }
}
