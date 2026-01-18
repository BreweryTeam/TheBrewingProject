package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.recipe.QualityDataBuilder;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.recipes.RecipeResultReader;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitRecipeResultReader implements RecipeResultReader<ItemStack> {
    @Override
    public QualityData<RecipeResult<ItemStack>> readRecipeResults(ConfigurationSection configurationSection) {
        QualityDataBuilder<RecipeResult<ItemStack>, BukkitRecipeResult.Builder> qualityDataBuilder = new QualityDataBuilder<>(BukkitRecipeResult.Builder::new);
        qualityDataBuilder.addOptional(configurationSection.getString("potion-attributes.custom-model-data"),
                Integer::parseInt,
                BukkitRecipeResult.Builder::customModelData
        );
        qualityDataBuilder.addOptionalString(configurationSection.getString("potion-attributes.item-model"), BukkitRecipeResult.Builder::itemModel);
        qualityDataBuilder.addOptionalStringList(configurationSection.getStringList("potion-attributes.lore"), BukkitRecipeResult.Builder::lore);
        qualityDataBuilder.addOptional(configurationSection.getString("potion-attributes.glint"), Boolean::parseBoolean, BukkitRecipeResult.Builder::glint);
        qualityDataBuilder.addString(
                configurationSection.getString("potion-attributes.name"),
                "Expected field 'potion-attributes.name'!",
                BukkitRecipeResult.Builder::name
        );
        qualityDataBuilder.add(
                configurationSection.getString("potion-attributes.color"),
                "Expected field 'potion-attributes.color'!",
                ColorUtil::parseColorString,
                BukkitRecipeResult.Builder::color
        );
        qualityDataBuilder.addOptional(
                configurationSection.getString("potion-attributes.append-brew-info-lore"),
                Boolean::parseBoolean,
                BukkitRecipeResult.Builder::appendBrewInfoLore
        );
        qualityDataBuilder.addOptionalString(configurationSection.getString("potion-attributes.custom-id"), BukkitRecipeResult.Builder::customId);
        qualityDataBuilder.add(getRecipeEffects(configurationSection), BukkitRecipeResult.Builder::recipeEffects);
        return qualityDataBuilder.build();
    }

    private static QualityData<RecipeEffectsImpl> getRecipeEffects(ConfigurationSection configurationSection) {
        QualityDataBuilder<RecipeEffectsImpl, RecipeEffectsImpl.Builder> qualityDataBuilder = new QualityDataBuilder<>(RecipeEffectsImpl.Builder::new);
        qualityDataBuilder.addOptionalString(configurationSection.getString("messages.action-bar"), RecipeEffectsImpl.Builder::actionBar);
        qualityDataBuilder.addOptionalString(configurationSection.getString("messages.title"), RecipeEffectsImpl.Builder::title);
        qualityDataBuilder.addOptionalString(configurationSection.getString("messages.message"), RecipeEffectsImpl.Builder::message);
        qualityDataBuilder.addOptionalList(configurationSection.getStringList("effects"), BukkitRecipeResultReader::getEffect, RecipeEffectsImpl.Builder::effects);
        qualityDataBuilder.addOptionalList(configurationSection.getStringList("events"), BreweryKey::parse, RecipeEffectsImpl.Builder::events);
        qualityDataBuilder.add(parseModifiers(configurationSection), RecipeEffectsImpl.Builder::addModifiers);
        return qualityDataBuilder.build();
    }

    private static QualityData<Map<DrunkenModifier, Double>> parseModifiers(ConfigurationSection configurationSection) {
        Map<DrunkenModifier, QualityData<Double>> modifierQualityMap = new HashMap<>();
        if (configurationSection.isConfigurationSection("modifiers")) {
            ConfigurationSection qualityDataSection = configurationSection.getConfigurationSection("modifiers");
            for (String modifier : qualityDataSection.getKeys(false)) {
                modifierQualityMap.put(DrunkenModifierSection.modifiers().modifier(modifier), QualityData.readQualityFactoredString(qualityDataSection.getString(modifier))
                        .map(Double::parseDouble)
                );
            }
        }
        QualityData<HashMap<DrunkenModifier, Double>> output = QualityData.fromValueMapper(ignored -> new HashMap<>());
        modifierQualityMap.forEach((modifier, qualityData) -> qualityData.forEach(((quality, aDouble) -> output.get(quality).put(modifier, aDouble))));
        return output.map(Map::copyOf);
    }

    private static RecipeEffectImpl getEffect(String string) {
        if (!string.contains("/")) {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.fromString(string.toLowerCase(Locale.ROOT)));
            Preconditions.checkNotNull(type);
            return new RecipeEffectImpl(type, new Interval(1, 1), new Interval(1, 1));
        }

        String[] parts = string.split("/");
        PotionEffectType type = PotionEffectType.getByName(parts[0]);
        Preconditions.checkNotNull(type, "invalid effect type: " + parts[0]);
        Interval durationBounds;
        Interval amplifierBounds;
        if (parts.length == 3) {
            durationBounds = Interval.parse(parts[2]).multiply(Moment.SECOND);
            amplifierBounds = Interval.parse(parts[1]);
        } else {
            if (type.isInstant()) {
                durationBounds = new Interval(1, 1);
                amplifierBounds = Interval.parse(parts[1]);
            } else {
                durationBounds = Interval.parse(parts[1]).multiply(Moment.SECOND);
                amplifierBounds = new Interval(0, 0);
            }
        }
        return new RecipeEffectImpl(type, durationBounds, amplifierBounds);
    }

}
