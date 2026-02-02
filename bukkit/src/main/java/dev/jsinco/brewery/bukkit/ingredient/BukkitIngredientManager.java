package dev.jsinco.brewery.bukkit.ingredient;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.ingredient.PluginIngredient;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.integration.IntegrationManagerImpl;
import dev.jsinco.brewery.recipes.BrewScoreImpl;
import io.papermc.paper.persistence.PersistentDataContainerView;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitIngredientManager implements IngredientManager<ItemStack> {

    public static final BukkitIngredientManager INSTANCE = new BukkitIngredientManager();

    @Override
    public Ingredient getIngredient(@NotNull ItemStack itemStack) {
        IntegrationManagerImpl integrationManager = TheBrewingProject.getInstance().getIntegrationManager();
        Ingredient ingredient = integrationManager.getIntegrationRegistry().getIntegrations(IntegrationTypes.ITEM)
                .stream()
                .filter(Integration::isEnabled)
                .map(integration -> integration.getIngredient(itemStack))
                .flatMap(Optional::stream)
                .findAny()
                .or(() -> BreweryIngredient.from(itemStack))
                .orElse(SimpleIngredient.from(itemStack));
        PersistentDataContainerView dataContainer = itemStack.getPersistentDataContainer();
        Double score = dataContainer.get(BrewAdapter.BREWERY_SCORE, PersistentDataType.DOUBLE);
        ImmutableMap.Builder<IngredientMeta<?>, Object> extraBuilder = new ImmutableMap.Builder<>();
        String displayNameString = dataContainer.get(BrewAdapter.BREWERY_DISPLAY_NAME, PersistentDataType.STRING);
        if (displayNameString != null) {
            Component displayName = MiniMessage.miniMessage().deserialize(displayNameString);
            extraBuilder.put(IngredientMeta.DISPLAY_NAME, displayName);
        }
        if (score != null) {
            extraBuilder.put(IngredientMeta.SCORE, score);
        }
        Map<IngredientMeta<?>, Object> extra = extraBuilder.build();
        return extra.isEmpty() ? ingredient : new IngredientWithMeta(ingredient, extraBuilder.build());
    }

    @Override
    public Optional<ItemStack> toItem(Ingredient ingredient) {
        double score;
        if (ingredient instanceof IngredientWithMeta ingredientWithMeta && ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double scoreOverride) {
            score = scoreOverride;
        } else {
            score = 1D;
        }
        Optional<ItemStack> itemStackOptional = switch (ingredient.toBaseIngredient()) {
            case SimpleIngredient(Material material) -> Optional.of(material.asItemType().createItemStack());
            case BreweryIngredient(BreweryKey breweryKey) -> TheBrewingProject.getInstance().getRecipeRegistry().getRecipe(breweryKey.minimalized())
                    .map(recipe -> {
                        RecipeResult<ItemStack> result = recipe.getRecipeResult(BrewScoreImpl.quality(score));
                        ItemStack itemStack = result.newLorelessItem();
                        itemStack.editPersistentDataContainer(pdc -> BrewAdapter.applyBrewTags(pdc, recipe, score, ""));
                        return itemStack;
                    });
            case PluginIngredient pluginIngredient ->
                    pluginIngredient.itemIntegration().createItem(pluginIngredient.getKey());
            default -> Optional.empty();
        };
        if (ingredient instanceof IngredientWithMeta ingredientWithMeta) {
            itemStackOptional.ifPresent(itemStack -> itemStack.editPersistentDataContainer(pdc -> {
                if (ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double scoreOverride) {
                    pdc.set(BrewAdapter.BREWERY_SCORE, PersistentDataType.DOUBLE, scoreOverride);
                }
                if (ingredientWithMeta.get(IngredientMeta.DISPLAY_NAME) instanceof Component displayName) {
                    pdc.set(BrewAdapter.BREWERY_DISPLAY_NAME, PersistentDataType.STRING, MiniMessage.miniMessage().serialize(displayName));
                }
            }));
        }
        return itemStackOptional;
    }

    @Override
    public CompletableFuture<Optional<Ingredient>> getIngredient(@NotNull String id) {
        BreweryKey breweryKey = BreweryKey.parse(id, Key.MINECRAFT_NAMESPACE);
        IntegrationManagerImpl integrationManager = TheBrewingProject.getInstance().getIntegrationManager();
        return integrationManager.getIntegrationRegistry().getIntegrations(IntegrationTypes.ITEM)
                .stream()
                .filter(Integration::isEnabled)
                .filter(itemIntegration -> itemIntegration.getId().equals(breweryKey.namespace()))
                .findAny()
                .map(itemIntegration -> itemIntegration.createIngredient(breweryKey.key()))
                .or(() -> BreweryIngredient.from(breweryKey))
                .or(() -> SimpleIngredient.from(id).map(Optional::of).map(CompletableFuture::completedFuture))
                .orElse(CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr) throws IllegalArgumentException {
        return getIngredientWithAmount(ingredientStr, false);
    }

    @Override
    public CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr, boolean withMeta) throws
            IllegalArgumentException {
        String[] ingredientSplit = ingredientStr.split("/");
        if (ingredientSplit.length > 2) {
            throw new IllegalArgumentException("Too many '/' separators for ingredientString, was: " + ingredientStr);
        }
        int amount;
        if (ingredientSplit.length == 1) {
            amount = 1;
        } else {
            amount = Integer.parseInt(ingredientSplit[1]);
        }
        return (withMeta ? this.deserializeIngredient(ingredientSplit[0]) : this.getIngredient(ingredientSplit[0]))
                .thenApplyAsync(ingredientOptional ->
                        ingredientOptional.map(ingredient -> new Pair<>(ingredient, amount))
                                .orElseThrow(() -> new IllegalArgumentException("Invalid ingredient string '" + ingredientStr + "' could not parse type"))
                );
    }

    @Override
    public CompletableFuture<Map<Ingredient, Integer>> getIngredientsWithAmount(List<String> stringList) throws IllegalArgumentException {
        return getIngredientsWithAmount(stringList, false);
    }


    @Override
    public CompletableFuture<Map<Ingredient, Integer>> getIngredientsWithAmount(List<String> stringList, boolean withMeta) throws
            IllegalArgumentException {
        if (stringList == null || stringList.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        Map<Ingredient, Integer> ingredientMap = new ConcurrentHashMap<>();
        CompletableFuture<?>[] ingredientsFuture = stringList.stream()
                .map(string -> getIngredientWithAmount(string, withMeta))
                .map(ingredientAmountPairFuture ->
                        ingredientAmountPairFuture
                                .thenAcceptAsync(ingredientAmountPair -> IngredientManager.insertIngredientIntoMap(ingredientMap, ingredientAmountPair))
                ).toArray(CompletableFuture<?>[]::new);
        return CompletableFuture.allOf(ingredientsFuture)
                .thenApplyAsync(ignored -> ingredientMap);
    }

    public boolean isValidIngredient(@NotNull String ingredientWithAmount) {
        try {
            this.getIngredientWithAmount(ingredientWithAmount);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
