package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeAttributeModifierTest {

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(strings = {"scale", "minecraft:scale", "SCALE", "generic.scale", "GENERIC_SCALE", "minecraft:generic.scale"})
    void parseAttribute(String name) {
        assertEquals(Attribute.SCALE, RecipeAttributeModifierImpl.parseAttribute(name));
    }

    @Test
    void parseAttribute_unknown() {
        assertThrows(IllegalArgumentException.class, () -> RecipeAttributeModifierImpl.parseAttribute("not_an_attribute"));
    }

    @Test
    void parseOperation() {
        assertEquals(AttributeModifier.Operation.ADD_NUMBER, RecipeAttributeModifierImpl.parseOperation("add_value"));
        assertEquals(AttributeModifier.Operation.ADD_NUMBER, RecipeAttributeModifierImpl.parseOperation("ADD_NUMBER"));
        assertEquals(AttributeModifier.Operation.ADD_SCALAR, RecipeAttributeModifierImpl.parseOperation("add_multiplied_base"));
        assertEquals(AttributeModifier.Operation.ADD_SCALAR, RecipeAttributeModifierImpl.parseOperation("add-scalar"));
        assertEquals(AttributeModifier.Operation.MULTIPLY_SCALAR_1, RecipeAttributeModifierImpl.parseOperation("multiply_scalar_1"));
        assertEquals(AttributeModifier.Operation.MULTIPLY_SCALAR_1, RecipeAttributeModifierImpl.parseOperation("add_scalar_1"));
        assertEquals(AttributeModifier.Operation.MULTIPLY_SCALAR_1, RecipeAttributeModifierImpl.parseOperation("minecraft:add_multiplied_total"));
        assertThrows(IllegalArgumentException.class, () -> RecipeAttributeModifierImpl.parseOperation("nonsense"));
    }

    @Test
    void parseConfig() {
        RecipeAttributeModifierImpl modifier = RecipeAttributeModifierImpl.parseConfig("gravity/-0.08;-0.04/30-60/add_value", "Moon Juice");
        assertEquals(Attribute.GRAVITY, modifier.attribute());
        assertEquals(AttributeModifier.Operation.ADD_NUMBER, modifier.operation());
        assertEquals(new RecipeAttributeModifierImpl.DoubleRange(-0.08, -0.04), modifier.amountRange());
        assertEquals(new Interval(600, 1200), modifier.durationRange());
        assertEquals(new NamespacedKey("brewery", "brew/moon_juice/minecraft/gravity/add_number"), modifier.key());

        RecipeAttributeModifierImpl defaultOperation = RecipeAttributeModifierImpl.parseConfig("minecraft:scale/0.5/10", "tiny");
        assertEquals(AttributeModifier.Operation.ADD_NUMBER, defaultOperation.operation());
        assertEquals(new Interval(200, 200), defaultOperation.durationRange());

        assertThrows(IllegalArgumentException.class, () -> RecipeAttributeModifierImpl.parseConfig("scale/0.5", "tiny"));

        RecipeAttributeModifierImpl manualKey = RecipeAttributeModifierImpl.parseConfig("scale/0.5/10/add_value/low_gravity", "tiny");
        assertEquals(new NamespacedKey("brewery", "low_gravity"), manualKey.key());
        RecipeAttributeModifierImpl manualNamespacedKey = RecipeAttributeModifierImpl.parseConfig("scale/0.5/10/add_value/myserver:Low_Gravity", "tiny");
        assertEquals(new NamespacedKey("myserver", "low_gravity"), manualNamespacedKey.key());

        assertEquals(
                RecipeAttributeModifierImpl.parseConfig("scale/0.5/10", "tiny").key(),
                RecipeAttributeModifierImpl.parseConfig("scale/0.2/20", "tiny").key()
        );
        assertNotEquals(
                RecipeAttributeModifierImpl.parseConfig("scale/0.5/10", "tiny").key(),
                RecipeAttributeModifierImpl.parseConfig("scale/0.5/10", "huge").key()
        );
    }

    @Test
    void pdcRoundTrip() {
        RecipeAttributeModifierImpl modifier = RecipeAttributeModifierImpl.parseConfig("gravity/-0.08;-0.04/30-60/add_multiplied_base/shared", "moon_juice");
        RecipeAttributeModifierPersistentDataType type = new RecipeAttributeModifierPersistentDataType();
        PersistentDataAdapterContext context = () -> null;
        assertEquals(modifier, type.fromPrimitive(type.toPrimitive(modifier, context), context));
    }
}
