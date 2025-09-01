package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.effect.text.DrunkTextElement;
import dev.jsinco.brewery.effect.text.SingleDrunkTextElement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SingleDrunkTextElementTest {


    @Test
    void findTransform_empty() {
        SingleDrunkTextElement singleDrunkTextElement = new SingleDrunkTextElement("*hic*", 100, new DrunkenModifier(
                "alcohol",
                ModifierExpression.ZERO,
                ModifierExpression.ZERO,
                0D), 0D);
        assertFalse(singleDrunkTextElement.findTransform("").isEmpty(), "Did not insert any drunk text");
    }

    @Test
    void findTransform() {
        SingleDrunkTextElement singleDrunkTextElement = new SingleDrunkTextElement("*hic*", 100, new DrunkenModifier(
                "alcohol",
                ModifierExpression.ZERO,
                ModifierExpression.ZERO,
                0D), 0D);
        List<DrunkTextElement.TextTransformation> transformed = singleDrunkTextElement.findTransform(" ");
        assertFalse(transformed.isEmpty(), "Did not insert any drunk text");
    }
}