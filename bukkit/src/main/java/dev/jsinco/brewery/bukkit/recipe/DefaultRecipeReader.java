package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class DefaultRecipeReader {


    public static Map<String, RecipeResult<ItemStack>> readDefaultRecipes(File folder) {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("default-recipes");
        ImmutableMap.Builder<String, RecipeResult<ItemStack>> recipes = new ImmutableMap.Builder<>();
        for (String recipeName : recipesSection.getKeys(false)) {
            recipes.put(recipeName, getDefaultRecipe(recipesSection.getConfigurationSection(recipeName)));
        }
        return recipes.build();
    }

    public static BukkitRecipeResult getDefaultRecipe(ConfigurationSection defaultRecipe) {
        return new BukkitRecipeResult.Builder()
                .name(defaultRecipe.getString("name", "Cauldron Brew"))
                .lore(defaultRecipe.getStringList("lore"))
                .color(ColorUtil.parseColorString(defaultRecipe.getString("color", "BLUE")))
                .customModelData(defaultRecipe.getInt("custom-model-data", -1))
                .glint(defaultRecipe.getBoolean("glint", false))
                .recipeEffects(RecipeEffects.GENERIC)
                .appendBrewInfoLore(false)
                .build();
    }
}
