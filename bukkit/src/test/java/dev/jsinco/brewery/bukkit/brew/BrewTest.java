package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.util.CollectionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockBukkitExtension.class)
public class BrewTest {

    @Test
    void brewers_empty() {
        Brew brew = new BrewImpl(List.of());
        assertTrue(brew.getBrewers().isEmpty());
    }

    @Test
    void brewers_collectFromSteps() {
        Brew brew = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
                        ),
                        new DistillStepImpl(
                                3,
                                CollectionUtil.sequencedSetOf(UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"))
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA,
                                CollectionUtil.sequencedSetOf(UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919"))
                        )
                )
        );
        SequencedSet<UUID> expected = CollectionUtil.sequencedSetOf(
                UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"),
                UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919")
        );
        // converting to list tests iteration order instead of set equality
        assertEquals(new ArrayList<>(expected), new ArrayList<>(brew.getBrewers()));
    }

    @Test
    void brewers_respectsStepOrder() {
        Brew brew = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(
                                        UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                                        UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919")
                                )
                        ),
                        new DistillStepImpl(
                                3,
                                CollectionUtil.sequencedSetOf(UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"))
                        ),
                        new AgeStepImpl(
                                new PassedMoment(20),
                                BarrelType.ACACIA,
                                CollectionUtil.sequencedSetOf(UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919"))
                        )
                )
        );
        SequencedSet<UUID> expected = CollectionUtil.sequencedSetOf(
                UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919"),
                UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4")
        );
        // converting to list tests iteration order instead of set equality
        assertEquals(new ArrayList<>(expected), new ArrayList<>(brew.getBrewers()));
    }

    @Test
    void withStepsReplaced() {
        List<BrewingStep> steps = List.of(
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
        );
        Brew brew = new BrewImpl(List.of(new DistillStepImpl(1)));
        assertEquals(steps, brew.withStepsReplaced(steps).getSteps());
    }

    void withModifiedStep() {
        List<BrewingStep> expected = List.of(
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
        );
        List<BrewingStep> steps = List.of(
                new CookStepImpl(
                        new PassedMoment(20),
                        Map.of(SimpleIngredient.from("wheat").get(), 1),
                        CauldronType.LAVA
                ),
                new DistillStepImpl(
                        1
                ),
                new AgeStepImpl(
                        new PassedMoment(20),
                        BarrelType.ACACIA
                )
        );
        Brew brew = new BrewImpl(steps);
        assertEquals(expected, brew.withModifiedStep(1, step -> new DistillStepImpl(3)));
    }

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
    void equals2() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
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
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
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
    void equals_notEqual2() {
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

    @Test
    void equals_notEqual3() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA
                        ),
                        new DistillStepImpl(
                                3,
                                CollectionUtil.sequencedSetOf(UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"))
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
        assertNotEquals(brew1, brew2);
    }

    @Test
    void equals_notEqual4() {
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(
                                        UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af"),
                                        UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4")
                                )
                        )
                )
        );
        BrewImpl brew2 = new BrewImpl(
                List.of(
                        new CookStepImpl(
                                new PassedMoment(20),
                                Map.of(SimpleIngredient.from("wheat").get(), 1),
                                CauldronType.LAVA,
                                CollectionUtil.sequencedSetOf(
                                        // reversed order
                                        UUID.fromString("144ce39d-301b-40a9-9788-0ca8cb23daf4"),
                                        UUID.fromString("f6489b79-7a9f-49e2-980e-265a05dbc3af")
                                )
                        )
                )
        );
        assertNotEquals(brew1, brew2);
    }
}
