package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.recipe.RecipeResult;
import dev.jsinco.brewery.recipe.QualityData;
import dev.jsinco.brewery.recipes.RecipeReader;
import dev.jsinco.brewery.recipes.RecipeResultReader;
import dev.jsinco.brewery.util.BreweryKey;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;

public class BukkitRecipeResultReader implements RecipeResultReader<ItemStack> {
    @Override
    public QualityData<RecipeResult<ItemStack>> readRecipeResults(ConfigurationSection configurationSection) {
        QualityData<RecipeEffects> recipeEffects = getRecipeEffects(configurationSection);
        QualityData<Integer> customModelData = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.custom-model-data"))
                .map(Integer::parseInt);
        QualityData<String> itemModel = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.item-model"));
        QualityData<List<String>> lore = QualityData.readQualityFactoredStringList(configurationSection.getStringList("potion-attributes.lore"));
        QualityData<Boolean> glint = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.glint", "false"))
                .map(Boolean::parseBoolean);
        QualityData<String> names = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.name"));
        QualityData<Color> colors = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.color"))
                .map(ColorUtil::parseColorString);
        QualityData<Boolean> appendBrewInfoLore = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.append-brew-info-lore", "true"))
                .map(Boolean::parseBoolean);
        QualityData<String> customId = QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.custom-id"));

        return QualityData.fromValueMapper(brewQuality ->
                new BukkitRecipeResult.Builder()
                        .name(names.get(brewQuality))
                        .recipeEffects(recipeEffects.get(brewQuality))
                        .lore(lore.get(brewQuality))
                        .glint(glint.get(brewQuality))
                        .color(colors.get(brewQuality))
                        .appendBrewInfoLore(appendBrewInfoLore.get(brewQuality))
                        .customId(customId.get(brewQuality))
                        .customModelData(customModelData.get(brewQuality))
                        .itemModel(itemModel.get(brewQuality))
                        .build()
        );
    }

    private static QualityData<RecipeEffects> getRecipeEffects(ConfigurationSection configurationSection) {
        QualityData<String> actionBar = QualityData.readQualityFactoredString(configurationSection.getString("messages.action-bar", null));
        QualityData<String> title = QualityData.readQualityFactoredString(configurationSection.getString("messages.title", null));
        QualityData<String> message = QualityData.readQualityFactoredString(configurationSection.getString("messages.message", null));
        QualityData<List<RecipeEffect>> effects = QualityData.readQualityFactoredStringList(configurationSection.getStringList("effects"))
                .map(list -> list
                        .stream()
                        .map(BukkitRecipeResultReader::getEffect)
                        .toList()
                );
        QualityData<List<BreweryKey>> events = QualityData.readQualityFactoredStringList(configurationSection.getStringList("events"))
                .map(list -> list
                        .stream()
                        .map(BreweryKey::parse)
                        .toList()
                );
        QualityData<Integer> alcohol = QualityData.readQualityFactoredString(configurationSection.getString("alcohol", "0%"))
                .map(RecipeReader::parseAlcoholString);
        return QualityData.fromValueMapper(quality -> new RecipeEffects.Builder()
                .actionBar(actionBar.get(quality))
                .title(title.get(quality))
                .message(message.get(quality))
                .alcohol(alcohol.getOrDefault(quality, 0))
                .effects(effects.getOrDefault(quality, List.of()))
                .events(events.getOrDefault(quality, List.of()))
                .build()
        );
    }

    private static RecipeEffect getEffect(String string) {
        if (!string.contains("/")) {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.fromString(string.toLowerCase(Locale.ROOT)));
            Preconditions.checkNotNull(type);
            return new RecipeEffect(type, new Interval(1, 1), new Interval(1, 1));
        }

        String[] parts = string.split("/");
        PotionEffectType type = PotionEffectType.getByName(parts[0]);
        Preconditions.checkNotNull(type, "invalid effect type: " + parts[0]);
        Interval durationBounds;
        Interval amplifierBounds;
        if (parts.length == 3) {
            durationBounds = Interval.parse(parts[2]);
            amplifierBounds = Interval.parse(parts[1]);
        } else {
            durationBounds = Interval.parse(parts[1]);
            amplifierBounds = new Interval(1, 1);
        }
        return new RecipeEffect(type, durationBounds, amplifierBounds);
    }

}
