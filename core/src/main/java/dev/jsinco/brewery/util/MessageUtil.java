package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
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
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    public static TagResolver getScoreTagResolver(@NonNull BrewScore score) {
        BrewQuality quality = score.brewQuality();
        return TagResolver.resolver(
                Placeholder.component("quality", score.displayName()),
                Placeholder.styling("quality_color", resolveQualityColor(quality))
        );
    }

    public static @NonNull TagResolver getBrewStepTagResolver(BrewingStep brewingStep, Map<ScoreType, PartialBrewScore> scores, double difficulty) {
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
                    Placeholder.component("cauldron_type", Component.translatable("tbp.cauldron.type." + cook.cauldronType().name().toLowerCase(Locale.ROOT)))
            );
            case BrewingStep.Distill distill -> TagResolver.resolver(
                    Formatter.number("distill_runs", distill.runs()),
                    Placeholder.unparsed("distill_runs_numerals", NumberFormatting.toRoman(distill.runs()))
            );
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

    public static @NonNull StyleBuilderApplicable[] resolveQualityColor(@Nullable BrewQuality quality) {
        if (quality != null) {
            TextColor translatedColor = getTranslatedColor(quality.colorKey());
            TextColor color = translatedColor != null ? translatedColor : TextColor.color(quality.getColor());
            return new StyleBuilderApplicable[]{color};
        }
        return new StyleBuilderApplicable[]{NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH};
    }

    public static @NonNull Stream<Component> compileBrewInfo(Brew brew, BrewScore score, boolean detailed) {
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

    public static @NonNull Stream<Component> compileBrewInfo(Brew brew, boolean detailed, RecipeRegistry<?> registry) {
        BrewScore score = brew.closestRecipe(registry)
                .map(brew::score)
                .orElse(BrewScoreImpl.failed(brew));
        return compileBrewInfo(brew, score, detailed);
    }

    public static @NonNull TagResolver getValueDisplayTagResolver(double displayValue) {
        return TagResolver.resolver(
                Placeholder.component("bars", compileBars(displayValue)),
                Placeholder.component("skulls", compileSkulls(displayValue)),
                Placeholder.component("stars", compileStars(displayValue))
        );
    }

    private static @NonNull ComponentLike compileSkulls(double level) {
        Component skull = getTranslatedComponent("tbp.brew.skull", Component.text(SKULL));
        TextColor skullColor = getTranslatedColor("tbp.brew.skull.color");

        int partition = (int) level / 20;
        Component result = Component.empty();
        for (int i = 0; i < partition; i++) result = result.append(skull);
        for (int i = 0; i < 5 - partition; i++) result = result.append(Component.text("  "));
        return result.colorIfAbsent(skullColor != null ? skullColor : NamedTextColor.GREEN);
    }

    private static @NonNull ComponentLike compileBars(double level) {
        TextColor ok = getTranslatedColor("tbp.brew.bar.color.ok");
        TextColor warning = getTranslatedColor("tbp.brew.bar.color.warning");
        TextColor severe = getTranslatedColor("tbp.brew.bar.color.severe");
        TextColor empty = getTranslatedColor("tbp.brew.bar.color.empty");

        int partitionedLevel = (int) level / 5;
        return Component.text("|".repeat(Math.min(partitionedLevel, 4))).color(ok != null ? ok : NamedTextColor.GREEN)
                .append(Component.text("|".repeat(Math.max(Math.min(partitionedLevel, 16) - 4, 0))).color(warning != null ? warning : NamedTextColor.YELLOW))
                .append(Component.text("|".repeat(Math.max(partitionedLevel - 16, 0))).color(severe != null ? severe : NamedTextColor.GOLD))
                .append(Component.text("|".repeat(20 - partitionedLevel)).color(empty != null ? empty : NamedTextColor.BLACK));
    }

    private static @NonNull Component compileStars(double level) {
        Component fullStar = getTranslatedComponent("tbp.brew.star.full", Component.text(FULL_STAR));
        Component halfStar = getTranslatedComponent("tbp.brew.star.half", Component.text(HALF_STAR));
        Component emptyStar = getTranslatedComponent("tbp.brew.star.empty", Component.text(EMPTY_STAR));

        int score = (int) (level / 10);
        int fullStars = score / 2;
        int remainder = score % 2;

        Component result = Component.empty();
        for (int i = 0; i < fullStars; i++) result = result.append(fullStar);
        if (remainder == 1) {
            result = result.append(halfStar);
            for (int i = 0; i < 4 - fullStars; i++) result = result.append(emptyStar);
        } else {
            for (int i = 0; i < 5 - fullStars; i++) result = result.append(emptyStar);
        }
        return result;
    }

    public static @NonNull TagResolver getTimeTagResolver(long timeTicks) {
        long cookingMinuteTicks = Config.config().cauldrons().cookingMinuteTicks();
        long seconds = (timeTicks % cookingMinuteTicks) * 60 / cookingMinuteTicks;
        long minutes = timeTicks / cookingMinuteTicks;
        return Placeholder.parsed("time", String.format("%d:%02d", minutes, seconds));
    }

    public static TagResolver numberedModifierTagResolver(@NonNull Map<DrunkenModifier, Double> modifiers, @Nullable String prefix) {
        TagResolver.Builder builder = TagResolver.builder();
        for (DrunkenModifier modifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            double value = modifiers.getOrDefault(modifier, modifier.minValue());
            builder.resolver(Formatter.number((prefix == null ? "" : prefix + "_") + modifier.name(), value));
        }
        return builder.build();
    }

    private static @NonNull Component getTranslatedComponent(String key, Component fallback) {
        Component rendered = GlobalTranslator.render(Component.translatable(key), Config.config().language());
        return rendered instanceof TranslatableComponent ? fallback : rendered;
    }

    private static @Nullable TextColor getTranslatedColor(String key) {
        Component rendered = GlobalTranslator.render(Component.translatable(key), Config.config().language());
        if (rendered instanceof TranslatableComponent) {
            return null;
        }
        Style style = rendered.style();
        if (style.color() != null) {
            return style.color();
        }
        if (!rendered.children().isEmpty()) {
            return rendered.children().get(0).color();
        }
        return null;
    }
}