package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.recipes.DefaultRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class DefaultRecipeReader {


    public static Map<String, DefaultRecipe<ItemStack, PotionMeta>> readDefaultRecipes(File folder) {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        ImmutableMap.Builder<String, DefaultRecipe<ItemStack, PotionMeta>> recipes = new ImmutableMap.Builder<>();
        for (String recipeName : recipesSection.getKeys(false)) {
            recipes.put(recipeName, getDefaultRecipe(recipesSection.getConfigurationSection(recipeName)));
        }
        return recipes.build();
    }

    public static BukkitDefaultRecipe getDefaultRecipe(ConfigurationSection defaultRecipe) {
        return new BukkitDefaultRecipe.Builder()
                .name(defaultRecipe.getString("name", "Cauldron Brew"))
                .lore(defaultRecipe.getStringList("lore"))
                .color(ColorUtil.parseColorString(defaultRecipe.getString("color", "BLUE")))
                .customModelData(defaultRecipe.getInt("custom-model-data", -1))
                .glint(defaultRecipe.getBoolean("glint", false))
                .build();
    }
}
