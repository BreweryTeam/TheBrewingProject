package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.effect.text.DrunkTextElement;
import dev.jsinco.brewery.effect.text.SingleDrunkTextElement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SingleDrunkTextElementTest {


    @Test
    void findTransform_empty() {
        SingleDrunkTextElement singleDrunkTextElement = new SingleDrunkTextElement("*hic*", 100, 0);
        assertFalse(singleDrunkTextElement.findTransform("").isEmpty(), "Did not insert any drunk text");
    }

    @Test
    void findTransform() {
        SingleDrunkTextElement singleDrunkTextElement = new SingleDrunkTextElement("*hic*", 100, 0);
        List<DrunkTextElement.TextTransformation> transformed = singleDrunkTextElement.findTransform(" ");
        assertFalse(transformed.isEmpty(), "Did not insert any drunk text");
    }
}