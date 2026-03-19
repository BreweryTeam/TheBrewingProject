package dev.jsinco.brewery.configuration;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.config.Configuration;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.ingredient.IngredientInput;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import dev.jsinco.brewery.api.ingredient.WildcardIngredient;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.Holder;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import net.kyori.adventure.text.format.NamedTextColor;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CauldronSection extends OkaeriConfig implements Configuration.Cauldrons {
    @Comment({"Reduce the number of particles that spawn while cauldrons brew.",
            "This won't affect performance, but it will make the particles less obtrusive."})
    @CustomKey("minimal-particles")
    private boolean minimalParticles = false;

    @Comment({"What blocks cauldrons must have below them to be able to brew.",
            "If this list is empty, cauldrons will brew regardless of the block below them.",
            "Campfires must be lit and lava must be a source block."})
    @CustomKey("heat-sources")
    private List<Holder.Material> heatSources = Stream.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block")
            .map(Holder.Material::fromMinecraftId)
            .toList();

    @Comment("How many ticks it will take to cook something one minute")
    @CustomKey("cooking-minute-ticks")
    private long cookingMinuteTicks = Moment.MINUTE;

    @Comment("The base color lava cauldrons have")
    @CustomKey("lava-base-particle-color")
    private Color lavaBaseParticleColor = new Color(Integer.parseInt("d45a12", 16));

    @Comment("The base color water cauldrons have")
    @CustomKey("water-base-particle-color")
    private Color waterBaseParticleColor = new Color(Integer.parseInt("3F76E4", 16));

    @Comment("The base color snow cauldrons have")
    @CustomKey("snow-base-particle-color")
    private Color snowBaseParticleColor = new Color(Integer.parseInt("f8fdfd", 16));

    @Comment("The base color snow cauldrons have")
    @CustomKey("failed-particle-color")
    private Color failedParticleColor = new Color(NamedTextColor.GRAY.value());

    @Comment("Whether to color the water in cauldrons using a text display")
    @CustomKey("colored-water")
    private boolean coloredWater = true;

    @Comment("The water color is a text display, this defines the opacity of the text display (0 - 255)")
    @CustomKey("water-color-opacity")
    private int waterColorOpacity = (128 & 0xFF);

    @Comment("To whom animations should be rendered when adding items [none, brewer, everyone]")
    @CustomKey("ingredient-added-animation-display")
    private AnimationDisplay ingredientAddedAnimation = AnimationDisplay.NONE;

    @Comment("How to display the time [action_bar, chat, title]")
    @CustomKey("clock-display")
    public final ModifierDisplay.DisplayWindow clockDisplay = ModifierDisplay.DisplayWindow.ACTION_BAR;

    @Comment("What items can be used to display time")
    @CustomKey("clock-items")
    public final List<IngredientInput> clockItems = List.of(UncheckedIngredient.minecraft("clock"));

    @Comment("What items should be transformed into another item when added as an ingredient")
    @CustomKey("ingredient-empty-transforms")
    private Map<IngredientInput, UncheckedIngredient> ingredientEmptyTransforms = new ImmutableMap.Builder<IngredientInput, UncheckedIngredient>()
            .put(WildcardIngredient.get("brewery:*"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("potion"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("lingering_potion"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("honey_bottle"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("ominous_bottle"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("dragons_breath"), UncheckedIngredient.minecraft("glass_bottle"))
            .put(UncheckedIngredient.minecraft("milk_bucket"), UncheckedIngredient.minecraft("bucket"))
            .put(UncheckedIngredient.minecraft("lava_bucket"), UncheckedIngredient.minecraft("bucket"))
            .put(UncheckedIngredient.minecraft("water_bucket"), UncheckedIngredient.minecraft("bucket"))
            .put(UncheckedIngredient.minecraft("powder_snow_bucket"), UncheckedIngredient.minecraft("bucket"))
            .put(UncheckedIngredient.minecraft("beetroot_soup"), UncheckedIngredient.minecraft("bowl"))
            .put(UncheckedIngredient.minecraft("mushroom_stew"), UncheckedIngredient.minecraft("bowl"))
            .put(UncheckedIngredient.minecraft("rabbit_stew"), UncheckedIngredient.minecraft("bowl"))
            .put(UncheckedIngredient.minecraft("suspicious_stew"), UncheckedIngredient.minecraft("bowl"))
            .build();

    public boolean minimalParticles() {
        return this.minimalParticles;
    }

    public List<Holder.Material> heatSources() {
        return this.heatSources;
    }

    public long cookingMinuteTicks() {
        return this.cookingMinuteTicks;
    }

    public Color lavaBaseParticleColor() {
        return this.lavaBaseParticleColor;
    }

    public Color waterBaseParticleColor() {
        return this.waterBaseParticleColor;
    }

    public Color snowBaseParticleColor() {
        return this.snowBaseParticleColor;
    }

    public Color failedParticleColor() {
        return this.failedParticleColor;
    }

    public boolean coloredWater() {
        return this.coloredWater;
    }

    public int waterColorOpacity() {
        return this.waterColorOpacity;
    }

    public AnimationDisplay ingredientAddedAnimation() {
        return this.ingredientAddedAnimation;
    }

    public Map<IngredientInput, UncheckedIngredient> ingredientEmptyTransforms() {
        return this.ingredientEmptyTransforms;
    }
}
