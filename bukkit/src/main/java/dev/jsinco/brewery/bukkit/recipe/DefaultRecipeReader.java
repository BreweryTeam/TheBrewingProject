package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.util.ColorUtil;
import dev.jsinco.brewery.recipes.RecipeConditionsReader;
import org.bukkit.inventory.ItemStack;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultRecipeReader {


    public static Map<String, CompletableFuture<DefaultRecipe<ItemStack>>> readDefaultRecipes(File folder) {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("incomplete-recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("incomplete-recipes");
        if (recipesSection == null) {
            return Map.of();
        }
        Map<String, CompletableFuture<DefaultRecipe<ItemStack>>> recipes = new HashMap<>();
        for (String recipeName : recipesSection.getKeys(false)) {
            try {
                BukkitRecipeResult bukkitRecipeResult = getDefaultRecipe(recipesSection.getConfigurationSection(recipeName));
                CompletableFuture<List<RecipeCondition>> recipeConditionsFuture;
                if (!recipesSection.isConfigurationSection(recipeName + ".condition")) {
                    recipeConditionsFuture = CompletableFuture.completedFuture(List.of());
                } else {
                    recipeConditionsFuture = RecipeConditionsReader.fromConfigSection(recipesSection.getConfigurationSection(recipeName + ".condition"), BukkitIngredientManager.INSTANCE);
                }
                recipes.put(recipeName, recipeConditionsFuture.thenApplyAsync(recipeConditions -> new DefaultRecipe<>(
                                bukkitRecipeResult,
                                recipeConditions,
                                recipesSection.getBoolean(recipeName + ".condition.for-ruined-brews", true)
                        )
                ));
            } catch (Throwable e) {
                Logger.logErr("Could not read default recipe: " + recipeName);
                Logger.logErr(e);
            }
        }
        return recipes;
    }

    public static BukkitRecipeResult getDefaultRecipe(ConfigurationSection defaultRecipe) {
        return new BukkitRecipeResult.Builder()
                .name(defaultRecipe.getString("name", "Cauldron Brew"))
                .lore(defaultRecipe.getStringList("lore"))
                .color(ColorUtil.parseColorString(defaultRecipe.getString("color", "BLUE")))
                .customModelData(defaultRecipe.getInt("custom-model-data", -1))
                .glint(defaultRecipe.getBoolean("glint", false))
                .recipeEffects(RecipeEffectsImpl.GENERIC)
                .appendBrewInfoLore(false)
                .build();
    }
}
