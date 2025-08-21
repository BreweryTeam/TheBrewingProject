package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class BarrelSection extends OkaeriConfig {

    @Comment("How many ticks it will take to age a brew one year")
    @CustomKey("aging-year-ticks")
    private long agingYearTicks = Moment.DEFAULT_AGING_YEAR;

    @Comment("Should players only be able to create barrels with a sign that has a keyword on the first line?")
    @CustomKey("require-sign-keyword")
    private boolean requireSignKeyword = true;

    @Comment("For what keywords should we check? (case insensitive)")
    @CustomKey("sign-keywords")
    private List<String> signKeywords = List.of("barrel");

}
