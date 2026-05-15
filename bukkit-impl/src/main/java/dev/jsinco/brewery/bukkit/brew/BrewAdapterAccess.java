package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.meta.MetaDataPdcType;
import dev.jsinco.brewery.bukkit.recipe.RecipeMatcherImpl;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.util.ClassUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class BrewAdapterAccess {


    private static final int DATA_VERSION = 0;
    private static final Random RANDOM = new Random();

    private static final NamespacedKey BREWING_STEPS = TheBrewingProject.key("steps");
    private static final NamespacedKey BREWERY_DATA_VERSION = TheBrewingProject.key("version");
    private static final NamespacedKey BREWERY_CIPHERED = TheBrewingProject.key("ciphered");
    private static final NamespacedKey BREWERY_META = TheBrewingProject.key("meta");
    public static final NamespacedKey BREWERY_TAG = TheBrewingProject.key("tag");
    public static final NamespacedKey BREWERY_SCORE = TheBrewingProject.key("score");
    public static final NamespacedKey BREWERY_DISPLAY_NAME = TheBrewingProject.key("display_name");

    public static ItemStack toItem(Brew brew, Brew.State state) {
        return RecipeMatcherImpl.builder()
                .build()
                .match(brew)
                .toItem(state);
    }


    public static void applyBrewData(PersistentDataContainer pdc, Brew brew) {
        pdc.set(BREWERY_DATA_VERSION, PersistentDataType.INTEGER, DATA_VERSION);
        if (Config.config().encryptSensitiveData()) {
            pdc.set(BREWERY_CIPHERED, PersistentDataType.BOOLEAN, true);
            pdc.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_CIPHERED_LIST, brew.getSteps());
        } else {
            pdc.remove(BREWERY_CIPHERED);
            pdc.set(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST, brew.getSteps());
        }
        pdc.set(BREWERY_META, MetaDataPdcType.INSTANCE, brew.meta());
    }

    public static Optional<Brew> fromItem(ItemStack itemStack) {
        PersistentDataContainerView data = itemStack.getPersistentDataContainer();
        Integer dataVersion = data.get(BREWERY_DATA_VERSION, PersistentDataType.INTEGER);
        if (!Objects.equals(dataVersion, DATA_VERSION)) {
            return Optional.empty();
        }
        List<BrewingStep> steps = data.has(BREWERY_CIPHERED, PersistentDataType.BOOLEAN)
                ? data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_CIPHERED_LIST)
                : data.get(BREWING_STEPS, ListPersistentDataType.BREWING_STEP_LIST);
        if (steps == null) {
            return Optional.empty();
        }
        MetaData meta = data.get(BREWERY_META, MetaDataPdcType.INSTANCE);
        if (meta == null) {
            meta = new MetaData();
        }
        return Optional.of(new BrewImpl(steps, meta));
    }

    public static Optional<DefaultRecipe<ItemStack>> getDefaultRecipe(@Nullable Recipe<ItemStack> recipe, RecipeRegistryImpl<ItemStack> recipeRegistry, Brew brew, boolean ruined) {
        List<DefaultRecipe<ItemStack>> allDefaultRecipes = new ArrayList<>(recipeRegistry.getDefaultRecipes());
        Collections.shuffle(allDefaultRecipes);
        return allDefaultRecipes
                .stream()
                .filter(defaultRecipe -> defaultRecipe.onlyRuinedBrews() == ruined)
                .filter(defaultRecipe -> defaultRecipe.recipeConditions().stream().allMatch(recipeCondition ->
                        recipeCondition.matches(Optional.ofNullable(recipe)
                                .map(Recipe::getSteps).orElse(null), brew.getCompletedSteps()))
                )
                .max(Comparator.comparing(DefaultRecipe::complexity));
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

    public static void applyBrewTags(PersistentDataContainer pdc, Recipe<ItemStack> recipe, double score, String miniMessageName) {
        pdc.set(BREWERY_TAG, PersistentDataType.STRING, BreweryKey.parse(recipe.getRecipeName()).minimalized());
        pdc.set(BREWERY_SCORE, PersistentDataType.DOUBLE, score);
        pdc.set(BREWERY_DISPLAY_NAME, PersistentDataType.STRING, miniMessageName);
    }

    public static boolean isBrew(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().has(BREWERY_TAG, PersistentDataType.STRING);
    }
}
