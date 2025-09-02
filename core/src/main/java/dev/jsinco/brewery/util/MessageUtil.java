package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.brew.*;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.format.TimeFormat;
import dev.jsinco.brewery.format.TimeFormatter;
import dev.jsinco.brewery.format.TimeModifier;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import net.kyori.adventure.audience.Audience;
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
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class MessageUtil {

    private static final char SKULL = '\u2620';
    private static final char FULL_STAR = '\u2605';
    private static final char HALF_STAR = '\u2BEA';
    private static final char EMPTY_STAR = '\u2606';

    public static Component miniMessage(String miniMessage, TagResolver... resolvers) {
        return MiniMessage.miniMessage().deserialize(miniMessage, resolvers);
    }

    public static void message(Audience audience, String translationKey, TagResolver... resolvers) {
        audience.sendMessage(Component.translatable(translationKey, Argument.tagResolver(resolvers)));
    }

    public static Component translated(String translationKey, TagResolver... resolvers) {
        return GlobalTranslator.render(
                Component.translatable(translationKey, Argument.tagResolver(resolvers)),
                Config.config().language()
        );
    }

    public static TagResolver getScoreTagResolver(@NotNull BrewScore score) {
        BrewQuality quality = score.brewQuality();
        return TagResolver.resolver(
                Placeholder.component("quality", score.displayName()),
                Placeholder.styling("quality_color", resolveQualityColor(quality))
        );
    }

    public static @NotNull TagResolver getBrewStepTagResolver(BrewingStep brewingStep, Map<PartialBrewScore.Type, PartialBrewScore> scores, double difficulty) {
        TagResolver resolver = switch (brewingStep) {
            case BrewingStep.Age age -> TagResolver.resolver(
                    Placeholder.component("barrel_type", GlobalTranslator.render(Component.translatable("tbp.barrel.type." + age.barrelType().name().toLowerCase(Locale.ROOT)), Config.config().language())),
                    Placeholder.parsed("aging_years", TimeFormatter.format(age.time().moment(), TimeFormat.AGING_YEARS, TimeModifier.AGING))
            );
            case BrewingStep.Cook cook -> TagResolver.resolver(
                    Placeholder.parsed("cooking_time", TimeFormatter.format(cook.time().moment(), TimeFormat.COOKING_TIME, TimeModifier.COOKING)),
                    Placeholder.component("ingredients", cook.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName()
                                    .append(Component.text("/" + entry.getValue()))
                            )
                            .collect(Component.toComponent(Component.text(", ")))
                    ),
                    Placeholder.component("cauldron_type", Component.translatable("cauldron.type." + cook.cauldronType().name().toLowerCase(Locale.ROOT)))
            );
            case BrewingStep.Distill distill -> Formatter.number("distill_runs", distill.runs());
            case BrewingStep.Mix mix -> TagResolver.resolver(
                    Placeholder.parsed("mixing_time", TimeFormatter.format(mix.time().moment(), TimeFormat.MIXING_TIME, TimeModifier.COOKING)),
                    Placeholder.component("ingredients", mix.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName()
                                    .append(Component.text("/" + entry.getValue()))
                            ).collect(Component.toComponent(Component.text(", ")))
                    )
            );
            default -> throw new IllegalStateException("Unexpected value: " + brewingStep);
        };
        return TagResolver.resolver(resolver, TagResolver.resolver(scores.values().stream()
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
            String translationKey = (detailed ? "tbp.brew.detailed-tooltip." : "tbp.brew.tooltip-brewing.") + brewingStep.stepType().name().toLowerCase(Locale.ROOT);
            streamBuilder.add(
                    Component.translatable(translationKey,
                            Argument.tagResolver(MessageUtil.getBrewStepTagResolver(brewingStep, score.getPartialScores(i), score.brewDifficulty()))
                    )
            );
        }
        return streamBuilder.build();
    }

    public static @NotNull Stream<Component> compileBrewInfo(Brew brew, boolean detailed, RecipeRegistry<?> registry) {
        BrewScore score = brew.closestRecipe(registry)
                .map(brew::score)
                .orElse(BrewScoreImpl.failed(brew));
        return compileBrewInfo(brew, score, detailed);
    }

    public static @NotNull TagResolver getValueDisplayTagResolver(double displayValue) {
        return TagResolver.resolver(
                Placeholder.component("bars", compileBars(displayValue)),
                Placeholder.component("skulls", compileSkulls(displayValue)),
                Placeholder.component("stars", compileStars(displayValue))
        );
    }

    private static @NotNull ComponentLike compileSkulls(double level) {
        int partition = (int) level / 20;
        StringBuilder skulls = new StringBuilder();
        skulls.repeat(SKULL, partition);
        skulls.repeat("  ", 5 - partition);
        return Component.text(skulls.toString()).color(NamedTextColor.GREEN);
    }

    private static @NotNull ComponentLike compileBars(double level) {
        int partitionedLevel = (int) level / 5;
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

    private static @NotNull Component compileStars(double level) {
        StringBuilder builder = new StringBuilder();
        int score = (int) (level / 10);
        int fullStars = score / 2;
        int remainder = score % 2;
        builder.repeat(FULL_STAR, fullStars);
        if (remainder == 1) {
            builder.append(HALF_STAR);
            builder.repeat(EMPTY_STAR, 4 - fullStars);
        } else {
            builder.repeat(EMPTY_STAR, 5 - fullStars);
        }
        return Component.text(builder.toString());
    }

    public static @NotNull TagResolver getTimeTagResolver(long timeTicks) {
        long cookingMinuteTicks = Config.config().cauldrons().cookingMinuteTicks();
        long seconds = (timeTicks % cookingMinuteTicks) * 60 / cookingMinuteTicks;
        long minutes = timeTicks / cookingMinuteTicks;
        return Placeholder.parsed("time", String.format("%d:%02d", minutes, seconds));
    }

    public static TagResolver numberedModifierTagResolver(@NotNull Map<DrunkenModifier, Double> modifiers, @Nullable String prefix) {
        TagResolver.Builder builder = TagResolver.builder();
        for (DrunkenModifier modifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            double value = modifiers.getOrDefault(modifier, modifier.minValue());
            builder.resolver(Formatter.number((prefix == null ? "" : prefix + "_") + modifier.name(), value));
        }
        return builder.build();
    }
}
