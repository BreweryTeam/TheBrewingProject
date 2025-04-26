package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewQuality;
import dev.jsinco.brewery.brew.BrewScore;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.recipes.RecipeImpl;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageUtil {

    private static final char SKULL = '\u2620';

    public static Component compilePlayerMessage(String message, Player player, DrunksManagerImpl<?> drunksManager, int alcohol) {
        DrunkStateImpl drunkState = drunksManager.getDrunkState(player.getUniqueId());
        return MiniMessage.miniMessage().deserialize(
                message,
                Placeholder.parsed("alcohol", String.valueOf(alcohol)),
                Placeholder.parsed("player_alcohol", String.valueOf(drunkState == null ? "0" : drunkState.alcohol())),
                getPlayerTagResolver(player)
        );
    }

    public static TagResolver getPlayerTagResolver(Player player) {
        return TagResolver.resolver(
                Placeholder.component("player_name", player.name()),
                Placeholder.component("team_name", player.teamDisplayName()),
                Placeholder.unparsed("world", player.getWorld().getName())
        );
    }

    public static TagResolver getScoreTagResolver(@NotNull BrewScore score) {
        BrewQuality quality = score.brewQuality();
        return TagResolver.resolver(
                Placeholder.component("quality", Component.text(score.displayName())),
                Placeholder.styling("quality_color", resolveQualityColor(quality))
        );
    }

    public static @NotNull TagResolver getBrewStepTagResolver(BrewingStep brewingStep, double score) {
        TagResolver resolver = switch (brewingStep) {
            case BrewingStep.Age age -> TagResolver.resolver(
                    Formatter.number("aging_years", age.age().agingYears()),
                    Placeholder.parsed("barrel_type", TranslationsConfig.BARREL_TYPE.get(age.barrelType().name().toLowerCase(Locale.ROOT)))
            );
            case BrewingStep.Cook cook -> TagResolver.resolver(
                    Formatter.number("cooking_time", cook.brewTime().minutes()),
                    Placeholder.parsed("ingredients", cook.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName() + "/" + entry.getValue())
                            .collect(Collectors.joining(", "))
                    ),
                    Placeholder.parsed("cauldron_type", TranslationsConfig.CAULDRON_TYPE.get(cook.cauldronType().name().toLowerCase(Locale.ROOT)))
            );
            case BrewingStep.Distill distill -> Formatter.number("distill_runs", distill.runs());
            case BrewingStep.Mix mix -> TagResolver.resolver(
                    Formatter.number("mixing_time", mix.time().minutes()),
                    Placeholder.parsed("ingredients", mix.ingredients().entrySet()
                            .stream()
                            .map(entry -> entry.getKey().displayName() + "/" + entry.getValue())
                            .collect(Collectors.joining(", "))
                    )
            );
        };
        return TagResolver.resolver(resolver, Placeholder.styling("partial_quality_color", resolveQualityColor(BrewScoreImpl.quality(score))));
    }

    private static @NotNull StyleBuilderApplicable[] resolveQualityColor(@Nullable BrewQuality quality) {
        return quality != null ? new StyleBuilderApplicable[]{TextColor.color(quality.getColor())} : new StyleBuilderApplicable[]{NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH};
    }

    public static TagResolver recipeTagResolver(RecipeImpl<ItemStack> recipe) {
        return TagResolver.resolver(
                Placeholder.parsed("recipe_name", recipe.getRecipeName()),
                Formatter.number("recipe_difficulty", recipe.getBrewDifficulty())
        );
    }

    public static TagResolver recipeEffectResolver(RecipeEffects effects) {
        return TagResolver.resolver(
                Placeholder.component("potion_effects", effects.getEffects().stream()
                        .map(effect ->
                                Component.translatable(effect.type().translationKey())
                                        .append(Component.text("/" + effect.durationRange() + "/" + effect.amplifierRange()))
                        ).collect(Component.toComponent(Component.text(",")))
                ),
                Formatter.number("effect_alcohol", effects.getAlcohol()),
                Formatter.number("effect_toxins", effects.getToxins()),
                Placeholder.parsed("effect_title_message", effects.getTitle() == null ? "" : effects.getTitle()),
                Placeholder.parsed("effect_message", effects.getMessage() == null ? "" : effects.getMessage()),
                Placeholder.parsed("effect_action_bar", effects.getActionBar() == null ? "" : effects.getActionBar()),
                Placeholder.parsed("effect_events", effects.getEvents().stream().map(drunkEvent -> {
                    if (drunkEvent instanceof NamedDrunkEvent namedDrunkEvent) {
                        return TranslationsConfig.EVENT_TYPES.get(namedDrunkEvent.name().toLowerCase(Locale.ROOT));
                    }
                    return drunkEvent.displayName();
                }).collect(Collectors.joining(",")))
        );
    }

    public static String formatIngredients(Map<Ingredient, Integer> ingredients) {
        return ingredients
                .entrySet()
                .stream()
                .map(entry -> entry.getKey().displayName() + "/" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    public static @NotNull Stream<Component> compileBrewInfo(Brew brew, BrewScore score, boolean detailed) {
        List<BrewingStep> brewingSteps = brew.getSteps();
        Stream.Builder<Component> streamBuilder = Stream.builder();
        for (int i = 0; i < brewingSteps.size(); i++) {
            BrewingStep brewingStep = brewingSteps.get(i);
            String line = (detailed ? TranslationsConfig.DETAILED_BREW_TOOLTIP : TranslationsConfig.BREW_TOOLTIP_BREWING).get(brewingStep.stepType().name().toLowerCase(Locale.ROOT));
            streamBuilder.add(MiniMessage.miniMessage().deserialize(line, MessageUtil.getBrewStepTagResolver(brewingStep, score.getPartialScore(i))));
        }
        return streamBuilder.build();
    }

    public static @NotNull Stream<Component> compileBrewInfo(Brew brew, boolean detailed) {
        BrewScore score = brew.closestRecipe(TheBrewingProject.getInstance().getRecipeRegistry())
                .map(brew::score)
                .orElse(BrewScoreImpl.NONE);
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
        long seconds = (timeTicks % Moment.MINUTE) / Moment.SECOND;
        long minutes = timeTicks / Moment.MINUTE;
        return Placeholder.parsed("time", String.format("%d:%02d", minutes, seconds));
    }
}
