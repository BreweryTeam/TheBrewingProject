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
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
            itemStack.editPersistentDataContainer(pdc -> {
                pdc.set(BREWERY_TAG, PersistentDataType.STRING, BreweryKey.parse(recipe.get().getRecipeName()).toString());
                pdc.set(BREWERY_SCORE, PersistentDataType.DOUBLE, score.get().score());
                if (recipeResult instanceof BukkitRecipeResult bukkitRecipeResult) {
                    pdc.set(BREWERY_DISPLAY_NAME, PersistentDataType.STRING, bukkitRecipeResult.getName());
                }
            });
        }
        if (!(state instanceof BrewImpl.State.Seal)) {
            itemStack.editPersistentDataContainer(pdc ->
                    fillPersistentData(pdc, brew)
            );
        }
        return itemStack;
    }

    private static ItemStack incompletePotion(Brew brew) {
        ItemStack itemStack = new ItemStack(Material.POTION);
        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hiddenComponents(Registry.DATA_COMPONENT_TYPE.stream()
                .filter(dataComponentType -> dataComponentType != DataComponentTypes.LORE)
                .collect(Collectors.toSet())
        ).build());
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
                BrewingStep.StepType.COOK, topIngredient == null ? TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_FERMENTED_UNKNOWN : TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_FERMENTED,
                BrewingStep.StepType.DISTILL, topIngredient == null ? TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_DISTILLED_UNKNOWN : TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_DISTILLED,
                BrewingStep.StepType.AGE, topIngredient == null ? TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_AGED_UNKNOWN : TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_AGED,
                BrewingStep.StepType.MIX, topIngredient == null ? TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_MIXED_UNKNOWN : TranslationsConfig.BREW_DISPLAY_NAME_UNFINISHED_MIXED
        );

        BrewingStep.StepType lastStep = brew.getCompletedSteps().getLast().stepType();
        Component displayName = topIngredient == null
                ? MessageUtil.miniMessage(displayNameByStep.get(lastStep))
                : MessageUtil.miniMessage(displayNameByStep.get(lastStep), Placeholder.component("ingredient", topIngredient.displayName()));

        itemStack.setData(DataComponentTypes.CUSTOM_NAME, displayName.decoration(TextDecoration.ITALIC, false));
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                .customColor(itemsInfo.first()).build());
        return itemStack;
    }

    private static void fillPersistentData(PersistentDataContainer pdc, Brew brew) {
        pdc.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST, brew.getSteps());
        pdc.set(BREWERY_DATA_VERSION, PersistentDataType.INTEGER, DATA_VERSION);
    }

    public static Optional<Brew> fromItem(ItemStack itemStack) {
        PersistentDataContainerView data = itemStack.getPersistentDataContainer();
        Integer dataVersion = data.get(BREWERY_DATA_VERSION, PersistentDataType.INTEGER);
        if (!Objects.equals(dataVersion, DATA_VERSION)) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST))
                .map(BrewImpl::new);
    }
}
