package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.IngredientUtil;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.recipe.RecipeResult;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BrewAdapter {


    private static final int DATA_VERSION = 0;

    private static final NamespacedKey BREWING_STEPS = BukkitAdapter.toNamespacedKey(BreweryKey.parse("steps"));
    private static final NamespacedKey BREWERY_DATA_VERSION = BukkitAdapter.toNamespacedKey(BreweryKey.parse("version"));
    public static final NamespacedKey BREWERY_TAG = BukkitAdapter.toNamespacedKey(BreweryKey.parse("tag"));
    public static final NamespacedKey BREWERY_SCORE = BukkitAdapter.toNamespacedKey(BreweryKey.parse("score"));
    public static final NamespacedKey BREWERY_DISPLAY_NAME = BukkitAdapter.toNamespacedKey(BreweryKey.parse("display_name"));

    public static ItemStack toItem(Brew brew, Brew.State state) {
        RecipeRegistryImpl<ItemStack> recipeRegistry = TheBrewingProject.getInstance().getRecipeRegistry();
        Optional<Recipe<ItemStack>> recipe = brew.closestRecipe(recipeRegistry);
        Optional<BrewScore> score = recipe.map(brew::score);
        Optional<BrewQuality> quality = score.flatMap(brewScore -> Optional.ofNullable(brewScore.brewQuality()));
        ItemStack itemStack;
        if (quality.isEmpty()) {
            RecipeResult<ItemStack> randomDefault = recipeRegistry.getRandomDefaultRecipe();
            //TODO Refactor this weird implementation for default recipes
            itemStack = randomDefault.newBrewItem(BrewScoreImpl.EXCELLENT, brew, state);
        } else if (!score.map(BrewScore::completed).get()) {
            itemStack = incompletePotion(brew);
        } else {
            RecipeResult<ItemStack> recipeResult = recipe.get().getRecipeResult(quality.get());
            itemStack = recipeResult.newBrewItem(score.get(), brew, state);
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(BREWERY_TAG, PersistentDataType.STRING, BreweryKey.parse(recipe.get().getRecipeName()).toString());
            meta.getPersistentDataContainer().set(BREWERY_SCORE, PersistentDataType.DOUBLE, score.get().score());
            if (recipeResult instanceof BukkitRecipeResult bukkitRecipeResult) {
                meta.getPersistentDataContainer().set(BREWERY_DISPLAY_NAME, PersistentDataType.STRING, bukkitRecipeResult.getName());
            }
            itemStack.setItemMeta(meta);
        }
        if (!(state instanceof BrewImpl.State.Seal)) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            fillPersistentData(itemMeta, brew);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private static ItemStack incompletePotion(Brew brew) {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        Map<Ingredient, Integer> ingredients = new HashMap<>();
        for (BrewingStep brewingStep : brew.getCompletedSteps()) {
            if (brewingStep instanceof BrewingStep.Cook cook) {
                IngredientManager.merge(ingredients, (Map<Ingredient, Integer>) cook.ingredients());
            }
            if (brewingStep instanceof BrewingStep.Mix mix) {
                IngredientManager.merge(ingredients, (Map<Ingredient, Integer>) mix.ingredients());
            }
        }
        Pair<org.bukkit.Color, Ingredient> itemsInfo = IngredientUtil.ingredientData(ingredients);
        Ingredient topIngredient = itemsInfo.second();

        final Map<BrewingStep.StepType, String> displayNameByStep = Map.of(
                BrewingStep.StepType.COOK, TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_FERMENTED_UNKNOWN,
                BrewingStep.StepType.DISTILL, TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_DISTILLED_UNKNOWN,
                BrewingStep.StepType.AGE, TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_AGED_UNKNOWN,
                BrewingStep.StepType.MIX, TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_MIXED_UNKNOWN
        );

        BrewingStep.StepType lastStep = brew.getCompletedSteps().getLast().stepType();
        Component displayName = topIngredient == null
                ? MiniMessage.miniMessage().deserialize(displayNameByStep.get(lastStep))
                : MiniMessage.miniMessage().deserialize(displayNameByStep.get(lastStep), Placeholder.component("ingredient", topIngredient.displayName()));

        potionMeta.customName(displayName.decoration(TextDecoration.ITALIC, false));
        potionMeta.setColor(itemsInfo.first());
        itemStack.setItemMeta(potionMeta);
        return itemStack;
    }

    private static boolean fillPersistentData(ItemMeta itemMeta, Brew brew) {
        PersistentDataContainer data = itemMeta.getPersistentDataContainer();
        Integer dataVersion = data.get(BREWERY_DATA_VERSION, PersistentDataType.INTEGER);
        boolean previouslyStored = dataVersion != null;
        data.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST, brew.getSteps());
        data.set(BREWERY_DATA_VERSION, PersistentDataType.INTEGER, DATA_VERSION);
        return previouslyStored;
    }

    public static Optional<Brew> fromItem(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        Integer dataVersion = data.get(BREWERY_DATA_VERSION, PersistentDataType.INTEGER);
        if (!Objects.equals(dataVersion, DATA_VERSION)) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST))
                .map(BrewImpl::new);
    }
}
