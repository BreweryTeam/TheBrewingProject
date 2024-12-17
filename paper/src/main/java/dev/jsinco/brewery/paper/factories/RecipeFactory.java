package dev.jsinco.brewery.paper.factories;

import dev.jsinco.brewery.paper.TheBrewingProject;
import dev.jsinco.brewery.paper.config.sections.RecipesConfig;
import dev.jsinco.brewery.api.enums.BarrelType;
import dev.jsinco.brewery.api.enums.CauldronType;
import dev.jsinco.brewery.api.enums.PotionQuality;
import dev.jsinco.brewery.paper.recipe.DefaultRecipe;
import dev.jsinco.brewery.paper.recipe.Recipe;
import dev.jsinco.brewery.paper.recipe.RecipeEffect;
import dev.jsinco.brewery.paper.recipe.ReducedRecipe;
import dev.jsinco.brewery.paper.recipe.ingredient.IngredientManager;
import dev.jsinco.brewery.paper.util.Pair;
import dev.jsinco.brewery.paper.util.Util;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeFactory {

    @Getter
    private final List<ReducedRecipe> reducedRecipes = new ArrayList<>();
    private final RecipesConfig recipesConfig;

    public RecipeFactory() {
        this.recipesConfig = TheBrewingProject.getInstance().getConfigManager().getRecipes();
    }


    /**
     * Obtain a ReducedRecipe from the recipes.yml file.
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A ReducedRecipe object with fewer attributes only used for identifying which recipe is being brewed
     */
    public ReducedRecipe getReducedRecipe(String recipeName) {
        RecipesConfig.ConfigurationRecipe recipe = recipesConfig.getRecipes().get(recipeName);

        return new ReducedRecipe.Builder(recipeName)
                // TODO: Use duration
                .brewTime(recipe.getBrewTime())
                .brewDifficulty(recipe.getBrewDifficulty())
                .cauldronType(Util.getEnumByName(CauldronType.class, recipe.getCauldronType()))
                .ingredients(IngredientManager.getIngredients(recipe.getIngredients()))
                .color(Util.parseColorString(recipe.getPotionAttributes().getColor()))
                .distillRuns(recipe.getDistilling().getRuns())
                .distillTime(recipe.getDistilling().getTime())
                .barrelType(Util.getEnumByName(BarrelType.class, recipe.getAging().getBarrelType()))
                .agingYears(recipe.getAging().getYears())
                .build();
    }


    /**
     * Obtain a recipe from the recipes.yml file.
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A Recipe object with all the attributes of the recipe.
     */
    public Recipe getRecipe(String recipeName) {
        ReducedRecipe reducedRecipe = getReducedRecipe(recipeName);

        return getRecipe(reducedRecipe);
    }

    /**
     * Get a full Recipe from a ReducedRecipe
     * @param reducedRecipe The ReducedRecipe to get the full Recipe from
     * @return A Recipe object with all the attributes of the recipe.
     */
    public Recipe getRecipe(ReducedRecipe reducedRecipe) {
        RecipesConfig.ConfigurationRecipe recipe = recipesConfig.getRecipes().get(reducedRecipe.getRecipeName());

        return new Recipe.Builder(reducedRecipe.getRecipeName())
                .brewTime(reducedRecipe.getBrewTime())
                .brewDifficulty(reducedRecipe.getBrewDifficulty())
                .alcohol(recipe.getAlcohol())
                .cauldronType(reducedRecipe.getCauldronType())
                .ingredients(reducedRecipe.getIngredients())
                .names(this.getQualityFactoredString(recipe.getPotionAttributes().getName()))
                .lore(this.getQualityFactoredList(recipe.getPotionAttributes().getLore()))
                .color(reducedRecipe.getColor())
                .glint(recipe.getPotionAttributes().getGlint())
                .customModelData(-1)
                .distillRuns(reducedRecipe.getDistillRuns())
                .distillTime(reducedRecipe.getDistillTime())
                .barrelType(reducedRecipe.getBarrelType())
                .agingYears(reducedRecipe.getAgingYears())
                .commands(this.getQualityFactoredList(recipe.getCommands()))
                .effects(this.getEffectsFromStringList(recipe.getEffects()))
                .title(recipe.getMessages().get("title"))
                .message(recipe.getMessages().get("chat"))
                .actionBar(recipe.getMessages().get("action-bar"))
                .build();
    }


    /**
     * Obtain a default recipe from the recipes.yml file.
     * Default recipes are used when no recipe is found while brewing.
     * @param defaultRecipeName The name/id of the default recipe to obtain. Ex: 'cauldron_brew'
     * @return A DefaultRecipe object with all the attributes of the default recipe.
     */
    public DefaultRecipe getDefaultRecipe(String defaultRecipeName) {
        RecipesConfig.ConfigurationDefaultRecipe defaultRecipe = recipesConfig.getDefaultRecipes().get(defaultRecipeName);

        return new DefaultRecipe.Builder()
                .name(defaultRecipe.getName())
                .lore(defaultRecipe.getLore())
                .color(Util.parseColorString(defaultRecipe.getColor()))
                .customModelData(-1)
                .glint(defaultRecipe.getGlint())
                .build();
    }

    public DefaultRecipe getRandomDefaultRecipe() {
        List<String> defaultRecipes = recipesConfig.getDefaultRecipes().keySet().stream().toList();
        return this.getDefaultRecipe(Util.getRandomElement(defaultRecipes));
    }

    // FIXME - I feel like there has to be a better way of doing this that doesn't rely on a map of enums?
    private Map<PotionQuality, String> getQualityFactoredString(String str) {
        if (!str.contains("/")) {
            return Map.of(PotionQuality.BAD, str, PotionQuality.GOOD, str, PotionQuality.EXCELLENT, str);
        }

        String[] list = str.split("/");
        Map<PotionQuality, String> map = new HashMap<>();

        for (int i = 0; i < Math.min(list.length, 3); i++) {
            map.put(PotionQuality.values()[i], list[i]);
        }

        return map;
    }

    private Map<PotionQuality, List<String>> getQualityFactoredList(List<String> list) {
        Map<PotionQuality, List<String>> map = new HashMap<>();

        for (String string : list) {
            if (string.startsWith("+")) {
                map.put(PotionQuality.BAD, list);
            } else if (string.startsWith("++")) {
                map.put(PotionQuality.GOOD, list);
            } else if (string.startsWith("+++")) {
                map.put(PotionQuality.EXCELLENT, list);
            } else {
                for (PotionQuality quality : PotionQuality.values()) {
                    map.put(quality, list);
                }
            }
        }

        return map;
    }

    private Pair<Integer, Integer> getNumberPair(String string) {
        if (!string.contains("-")) {
            int i = Util.getInt(string);
            return Pair.singleValue(i);
        }
        String[] split = string.split("-");
        return new Pair<>(Util.getInt(split[0]), Util.getInt(split[1]));
    }


    private List<RecipeEffect> getEffectsFromStringList(List<String> list) {
        List<RecipeEffect> effects = new ArrayList<>();
        for (String string : list) {
            effects.add(this.getEffect(string));
        }
        return effects;
    }


    private RecipeEffect getEffect(String string) {
        if (!string.contains("/")) {
            return RecipeEffect.of(PotionEffectType.getByName(string), Pair.singleValue(1), Pair.singleValue(1));
        }

        String[] parts = string.split("/");
        PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
        Pair<Integer, Integer> durationBounds = getNumberPair(parts[1]);
        Pair<Integer, Integer> amplifierBounds;
        if (parts.length >= 3) {
            amplifierBounds = getNumberPair(parts[2]);
        } else {
            amplifierBounds = Pair.singleValue(1);
        }

        return RecipeEffect.of(effectType, durationBounds, amplifierBounds);
    }


}
