package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.math.RangeD;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.jspecify.annotations.Nullable;

public record ParticleDefinition(BreweryKey particleKey, double probability, @Nullable RangeD range,
                                 @Nullable BrewQuality quality) {
}
