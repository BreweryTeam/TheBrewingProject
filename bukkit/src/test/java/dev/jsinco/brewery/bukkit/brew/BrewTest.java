package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.moment.PassedMoment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockBukkitExtension.class)
public class BrewTest {

    @Test
    void equals() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                3
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        BrewImpl brew2 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                3
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        assertEquals(brew1, brew2);
    }

    @Test
    void equals_notEqual() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                3
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        BrewImpl brew2 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                2
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        assertNotEquals(brew1, brew2);
    }

    @Test
    void equals_notEquals2() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                2
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        BrewImpl brew2 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 2),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                2
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA
                        )
                )
        );
        assertNotEquals(brew1, brew2);
    }
}
