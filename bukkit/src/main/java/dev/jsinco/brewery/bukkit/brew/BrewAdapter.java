package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.IngredientUtil;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.util.ClassUtil;
import dev.jsinco.brewery.api.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
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

    private static final NamespacedKey BREWING_STEPS = TheBrewingProject.key("steps");
    private static final NamespacedKey BREWERY_DATA_VERSION = TheBrewingProject.key("version");
    public static final NamespacedKey BREWERY_TAG = TheBrewingProject.key("tag");
    public static final NamespacedKey BREWERY_SCORE = TheBrewingProject.key("score");
    public static final NamespacedKey BREWERY_DISPLAY_NAME = TheBrewingProject.key("display_name");

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
        hideTooltips(itemStack);
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
                BrewingStep.StepType.COOK, "unfinished-fermented",
                BrewingStep.StepType.DISTILL, "unfinished-distilled",
                BrewingStep.StepType.AGE, "unfinished-aged",
                BrewingStep.StepType.MIX, "unfinished-mixed"
        );

        BrewingStep.StepType lastStep = brew.getCompletedSteps().getLast().stepType();
        String translationKey = "tbp.brew.display-name." + displayNameByStep.get(lastStep);
        Component displayName = topIngredient == null
                ? Component.translatable(translationKey + "-unknown")
                : Component.translatable(translationKey, Argument.tagResolver(Placeholder.component("ingredient", topIngredient.displayName())));

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

    public static void hideTooltips(ItemStack itemStack) {
        if (ClassUtil.exists("io.papermc.paper.datacomponent.item.TooltipDisplay")) {
            itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hiddenComponents(Registry.DATA_COMPONENT_TYPE.stream()
                            .filter(dataComponentType -> dataComponentType != DataComponentTypes.LORE)
                            .collect(Collectors.toSet())
                    ).build()
            );
        } else {
            itemStack.editMeta(meta -> meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM,
                    ItemFlag.HIDE_DYE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_STORED_ENCHANTS,
                    ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP));
        }
    }
}
