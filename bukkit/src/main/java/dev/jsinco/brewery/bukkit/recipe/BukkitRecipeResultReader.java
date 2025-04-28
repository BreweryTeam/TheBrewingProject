package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.recipes.QualityData;
import dev.jsinco.brewery.recipes.RecipeReader;
import dev.jsinco.brewery.recipes.RecipeResultReader;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.moment.Interval;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;

public class BukkitRecipeResultReader implements RecipeResultReader<ItemStack> {
    @Override
    public BukkitRecipeResult readRecipeResult(ConfigurationSection configurationSection) {
        return new BukkitRecipeResult.Builder()
                .recipeEffects(getRecipeEffects(configurationSection))
                .customModelData(configurationSection.getInt("potion-attributes.custom-model-data", -1))
                .itemModel(configurationSection.getString("potion-attributes.item-model", null))
                .lore(QualityData.readQualityFactoredStringList(configurationSection.getStringList("potion-attributes.lore")))
                .glint(configurationSection.getBoolean("potion-attributes.glint", false))
                .names(QualityData.readQualityFactoredString(configurationSection.getString("potion-attributes.name")))
                .color(ColorUtil.parseColorString(configurationSection.getString("potion-attributes.color")))
                .appendBrewInfoLore(configurationSection.getBoolean("potion-attributes.append-brew-info-lore", true))
                .customId(configurationSection.getString("potion-attributes.custom-id", null))
                .build();
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
        Interval durationBounds = Interval.parse(parts[1]);
        Interval amplifierBounds;
        if (parts.length >= 3) {
            amplifierBounds = Interval.parse(parts[2]);
        } else {
            amplifierBounds = new Interval(1, 1);
        }
        return new RecipeEffect(type, durationBounds, amplifierBounds);
    }

}
