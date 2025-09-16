package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.recipe.BukkitRecipeResult;
import dev.jsinco.brewery.bukkit.util.IngredientUtil;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.util.ClassUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class BrewAdapter {


    private static final int DATA_VERSION = 0;
    private static final Random RANDOM = new Random();

    private static final NamespacedKey BREWING_STEPS = TheBrewingProject.key("steps");
    private static final NamespacedKey BREWERY_DATA_VERSION = TheBrewingProject.key("version");
    private static final NamespacedKey BREWERY_CIPHERED = TheBrewingProject.key("ciphered");
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
            itemStack = fromDefaultRecipe(recipe, recipeRegistry, brew, state);
        } else if (!score.map(BrewScore::completed).get()) {
            Optional<DefaultRecipe<ItemStack>> defaultRecipeOptional = recipeRegistry.getDefaultRecipes().stream()
                    .filter(defaultRecipe -> !defaultRecipe.onlyRuinedBrews())
                    .filter(defaultRecipe -> defaultRecipe.recipeCondition().matches(recipe.get().getSteps(), brew.getSteps()))
                    .findAny();
            itemStack = defaultRecipeOptional.map(DefaultRecipe::result).map(result -> result.newBrewItem(score.get(), brew, state)).orElse(
                    incompletePotion(brew)
            );
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
                    applyBrewStepsData(pdc, brew)
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

        itemStack.setData(DataComponentTypes.CUSTOM_NAME, GlobalTranslator
                .render(displayName, Config.config().language()).decoration(TextDecoration.ITALIC, false));
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                .customColor(itemsInfo.first()).build());
        return itemStack;
    }

    private static ItemStack fromDefaultRecipe(Optional<Recipe<ItemStack>> recipe, RecipeRegistryImpl<ItemStack> recipeRegistry, Brew brew, Brew.State state) {
        List<DefaultRecipe<ItemStack>> defaultRecipes = recipeRegistry.getDefaultRecipes()
                .stream().filter(DefaultRecipe::onlyRuinedBrews)
                .filter(defaultRecipe ->
                        defaultRecipe.recipeCondition().complexity() > 1 && defaultRecipe.recipeCondition().matches(recipe.map(Recipe::getSteps).orElse(null), brew.getSteps())
                )
                .toList();
        if (defaultRecipes.isEmpty()) {
            defaultRecipes = recipeRegistry.getDefaultRecipes()
                    .stream().filter(DefaultRecipe::onlyRuinedBrews)
                    .filter(defaultRecipe ->
                            defaultRecipe.recipeCondition().complexity() > 0 && defaultRecipe.recipeCondition().matches(recipe.map(Recipe::getSteps).orElse(null), brew.getSteps())
                    )
                    .toList();
        }
        if (defaultRecipes.isEmpty()) {
            defaultRecipes = recipeRegistry.getDefaultRecipes()
                    .stream().filter(DefaultRecipe::onlyRuinedBrews)
                    .filter(defaultRecipe ->
                            defaultRecipe.recipeCondition().complexity() == 0
                    )
                    .toList();
        }
        if (defaultRecipes.isEmpty()) {
            ItemStack itemStack = new ItemStack(Material.POTION);
            itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Placeholder"));
            itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(
                    List.of(Component.text("you don't have any default/incomplete recipes!"),
                            Component.text("Contact admin, or if your admin look into incomplete-recipes.yml"))
            ));
            return itemStack;
        }

        return defaultRecipes.get(RANDOM.nextInt(defaultRecipes.size())).result().newBrewItem(BrewScoreImpl.PLACEHOLDER, brew, state);
    }

    public static void applyBrewStepsData(PersistentDataContainer pdc, Brew brew) {
        pdc.set(BREWERY_DATA_VERSION, PersistentDataType.INTEGER, DATA_VERSION);
        if (Config.config().encryptSensitiveData()) {
            pdc.set(BREWERY_CIPHERED, PersistentDataType.BOOLEAN, true);
            pdc.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_CIPHERED_LIST, brew.getSteps());
        } else {
            pdc.remove(BREWERY_CIPHERED);
            pdc.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST, brew.getSteps());
        }
    }

    public static Optional<Brew> fromItem(ItemStack itemStack) {
        PersistentDataContainerView data = itemStack.getPersistentDataContainer();
        Integer dataVersion = data.get(BREWERY_DATA_VERSION, PersistentDataType.INTEGER);
        if (!Objects.equals(dataVersion, DATA_VERSION)) {
            return Optional.empty();
        }
        if (data.has(BREWERY_CIPHERED, PersistentDataType.BOOLEAN)) {
            return Optional.ofNullable(data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_CIPHERED_LIST))
                    .map(BrewImpl::new);
        } else {
            return Optional.ofNullable(data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST))
                    .map(BrewImpl::new);
        }
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
