package dev.jsinco.brewery.bukkit;

import dev.faststats.Metrics;
import dev.faststats.data.Metric;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.structure.StructureType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    private static final Map<String, Integer> brewsMade = new ConcurrentHashMap<>();
    private static final Map<String, Integer> brewsDrunk = new ConcurrentHashMap<>();
    private static final Map<String, Integer> structuresMade = new ConcurrentHashMap<>();

    public static Metrics register(Metrics.Factory factory) {
        factory.addMetric(Metric.numberMap("brews_made", () -> brewsMade));
        factory.addMetric(Metric.numberMap("brews_drunk", () -> brewsDrunk));
        factory.addMetric(Metric.numberMap("structures_made", () -> structuresMade));
        factory.addMetric(Metric.number("recipes_count", () -> TheBrewingProject.getInstance().getRecipeRegistry().getRecipes().size()));
        factory.addMetric(Metric.stringArray("integrations", () -> TheBrewingProject.getInstance().getIntegrationManager().getIntegrationRegistry().getAllIntegrations()
                .stream()
                .map(Integration::getId)
                .distinct()
                .toArray(String[]::new)
        ));
        factory.onFlush(() -> {
            brewsDrunk.clear();
            brewsMade.clear();
            structuresMade.clear();
        });
        return factory.create();
    }

    public static void registerBrewMade(@Nullable BrewQuality quality) {
        brewsMade.compute(
                findName(quality),
                (ignored, value) -> value == null ? 1 : value + 1
        );
    }

    public static void registerBrewDrunk(@Nullable BrewQuality quality) {
        brewsDrunk.compute(
                findName(quality),
                (ignored, value) -> value == null ? 1 : value + 1
        );
    }

    private static String findName(@Nullable BrewQuality quality) {
        if (quality == null) {
            return "failed";
        } else {
            return quality.name().toLowerCase(Locale.ROOT);
        }
    }

    public static void registerStructureCreated(StructureType<?> type) {
        structuresMade.compute(
                type.key().minimalized(),
                (ignored, value) -> value == null ? 1 : value + 1
        );
    }
}
