package dev.jsinco.brewery.util;

import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.recipe.RecipeRegistry;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class MessageUtil {

    private static final char SKULL = '\u2620';

    public static TagResolver getScoreTagResolver(@NotNull BrewScore score) {
        BrewQuality quality = score.brewQuality();
        return TagResolver.resolver(
                Placeholder.component("quality", Component.text(score.displayName())),
                Placeholder.styling("quality_color", resolveQualityColor(quality))
        );
    }

    public static @NotNull TagResolver getBrewStepTagResolver(BrewingStep brewingStep, List<PartialBrewScore> scores, double difficulty) {
        TagResolver resolver = switch (brewingStep) {
            case BrewingStep.Age age -> TagResolver.resolver(
                    Placeholder.parsed("barrel_type", TranslationsConfig.BARREL_TYPE.get(age.barrelType().name().toLowerCase(Locale.ROOT))),
                    Formatter.number("aging_years", age.time().moment() / Config.config().barrels().agingYearTicks())
            );
            case BrewingStep.Cook cook -> TagResolver.resolver(
                    Formatter.number("cooking_time", cook.time().moment() / Config.config().cauldrons().cookingMinuteTicks()),
                    Placeholder.component("ingredients", cook.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName()
                                    .append(Component.text("/" + entry.getValue()))
                            )
                            .collect(Component.toComponent(Component.text(", ")))
                    ),
                    Placeholder.parsed("cauldron_type", TranslationsConfig.CAULDRON_TYPE.get(cook.cauldronType().name().toLowerCase(Locale.ROOT)))
            );
            case BrewingStep.Distill distill -> Formatter.number("distill_runs", distill.runs());
            case BrewingStep.Mix mix -> TagResolver.resolver(
                    Formatter.number("mixing_time", mix.time().moment() / Config.config().cauldrons().cookingMinuteTicks()),
                    Placeholder.component("ingredients", mix.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName()
                                    .append(Component.text("/" + entry.getValue()))
                            ).collect(Component.toComponent(Component.text(", ")))
                    )
            );
            default -> throw new IllegalStateException("Unexpected value: " + brewingStep);
        };
        return TagResolver.resolver(resolver, TagResolver.resolver(scores.stream()
                .map(partialBrewScore ->
                        Placeholder.styling(partialBrewScore.type().colorKey(), resolveQualityColor(
                                BrewScoreImpl.quality(BrewScoreImpl.applyDifficulty(partialBrewScore.score(), difficulty))
                        ))
                ).toArray(TagResolver[]::new))
        );
    }

    public static @NotNull StyleBuilderApplicable[] resolveQualityColor(@Nullable BrewQuality quality) {
        return quality != null ? new StyleBuilderApplicable[]{TextColor.color(quality.getColor())} : new StyleBuilderApplicable[]{NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH};
    }

    public static @NotNull Stream<Component> compileBrewInfo(Brew brew, BrewScore score, boolean detailed) {
        List<BrewingStep> brewingSteps = brew.getCompletedSteps();
        Stream.Builder<Component> streamBuilder = Stream.builder();
        for (int i = 0; i < brewingSteps.size(); i++) {
            BrewingStep brewingStep = brewingSteps.get(i);
            String line = (detailed ? TranslationsConfig.DETAILED_BREW_TOOLTIP : TranslationsConfig.BREW_TOOLTIP_BREWING).get(brewingStep.stepType().name().toLowerCase(Locale.ROOT));
            streamBuilder.add(MiniMessage.miniMessage().deserialize(line, MessageUtil.getBrewStepTagResolver(brewingStep, score.getPartialScores(i), score.brewDifficulty())));
        }
        return streamBuilder.build();
    }

    public static @NotNull Stream<Component> compileBrewInfo(Brew brew, boolean detailed, RecipeRegistry<?> registry) {
        BrewScore score = brew.closestRecipe(registry)
                .map(brew::score)
                .orElse(BrewScoreImpl.failed(brew));
        return compileBrewInfo(brew, score, detailed);
    }

    public static @NotNull TagResolver getDrunkStateTagResolver(@Nullable DrunkStateImpl drunkState) {
        return TagResolver.resolver(
                Placeholder.component("alcohol_level", compileAlcoholLevel(drunkState == null ? 0 : drunkState.alcohol())),
                Placeholder.component("toxins_level", compileToxinsLevel(drunkState == null ? 0 : drunkState.toxins()))
        );
    }

    private static @NotNull ComponentLike compileToxinsLevel(int level) {
        int partition = level / 20;
        StringBuilder skulls = new StringBuilder();
        skulls.repeat(SKULL, partition);
        skulls.repeat("  ", 5 - partition);
        return Component.text(skulls.toString()).color(NamedTextColor.GREEN);
    }

    private static @NotNull ComponentLike compileAlcoholLevel(int level) {
        int partitionedLevel = level / 5;
        StringBuilder okLevel = new StringBuilder();
        okLevel.repeat("|", Math.min(partitionedLevel, 4));
        StringBuilder warningLevel = new StringBuilder();
        warningLevel.repeat("|", Math.max(Math.min(partitionedLevel, 16) - 4, 0));
        StringBuilder severeLevel = new StringBuilder();
        severeLevel.repeat("|", Math.max(partitionedLevel - 16, 0));
        StringBuilder remainder = new StringBuilder();
        remainder.repeat("|", 20 - partitionedLevel);
        return Component.text(okLevel.toString()).color(NamedTextColor.GREEN)
                .append(Component.text(warningLevel.toString()).color(NamedTextColor.YELLOW))
                .append(Component.text(severeLevel.toString()).color(NamedTextColor.GOLD))
                .append(Component.text(remainder.toString()).color(NamedTextColor.BLACK));
    }

    public static @NotNull TagResolver getTimeTagResolver(long timeTicks) {
        long cookingMinuteTicks = Config.config().cauldrons().cookingMinuteTicks();
        long seconds = (timeTicks % cookingMinuteTicks) * 60 / cookingMinuteTicks;
        long minutes = timeTicks / cookingMinuteTicks;
        return Placeholder.parsed("time", String.format("%d:%02d", minutes, seconds));
    }
}
