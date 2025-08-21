package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.configuration.serializers.ConsumableSerializer;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class DecayRateSection extends OkaeriConfig {

    @Comment("How many ticks until alcohol level decays by 1%")
    @CustomKey("alcohol")
    private int alcohol = 200;

    @Comment("How many ticks until toxin level decays by 1%")
    @CustomKey("toxin")
    private int toxin = 400;

    @Comment("Items that should change alcohol/toxins when consumed")
    @CustomKey("consumables")
    private List<ConsumableSerializer.Consumable> consumables = List.of(
            new ConsumableSerializer.Consumable("ROTTEN_FLESH", 0, 3),
            new ConsumableSerializer.Consumable("SPIDER_EYE", 0, 2),
            new ConsumableSerializer.Consumable("MILK_BUCKET", -3, 0),
            new ConsumableSerializer.Consumable("BREAD", -2, -1)
    );

}
