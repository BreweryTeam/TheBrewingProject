package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.bukkit.integration.IntegrationManagerImpl;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitIngredientManager implements IngredientManager<ItemStack> {

    public static final BukkitIngredientManager INSTANCE = new BukkitIngredientManager();

    @Override
    public Ingredient getIngredient(@NotNull ItemStack itemStack) {
        IntegrationManagerImpl integrationManager = TheBrewingProject.getInstance().getIntegrationManager();
        return integrationManager.getIntegrationRegistry().getIntegrations(IntegrationTypes.ITEM)
                .stream()
                .filter(Integration::isEnabled)
                .map(integration -> integration.getIngredient(itemStack))
                .flatMap(Optional::stream)
                .findAny()
                .or(() -> BreweryIngredient.from(itemStack))
                .orElse(SimpleIngredient.from(itemStack));
    }


    @Override
    public CompletableFuture<Optional<Ingredient>> getIngredient(@NotNull String ingredientStr) {
        String id = ingredientStr.toLowerCase(Locale.ROOT);
        BreweryKey breweryKey = BreweryKey.parse(id);
        IntegrationManagerImpl integrationManager = TheBrewingProject.getInstance().getIntegrationManager();
        return integrationManager.getIntegrationRegistry().getIntegrations(IntegrationTypes.ITEM)
                .stream()
                .filter(Integration::isEnabled)
                .filter(itemIntegration -> itemIntegration.getId().equals(breweryKey.namespace()))
                .findAny()
                .map(itemIntegration -> itemIntegration.createIngredient(breweryKey.key()))
                .or(() -> BreweryIngredient.from(id).map(Optional::of).map(CompletableFuture::completedFuture))
                .or(() -> SimpleIngredient.from(id).map(Optional::of).map(CompletableFuture::completedFuture))
                .orElse(CompletableFuture.completedFuture(Optional.empty()));
    }

    /**
     * @param ingredientStr A string with the format [ingredient-name]/[runs]. Allows not specifying runs, where it will default to 1
     * @return An ingredient/runs pair
     * @throws IllegalArgumentException if the ingredients string is invalid
     */
    @Override
    public CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr) throws
            IllegalArgumentException {
        String[] ingredientSplit = ingredientStr.split("/");
        if (ingredientSplit.length > 2) {
            throw new IllegalArgumentException("To many '/' separators for ingredientString, was: " + ingredientStr);
        }
        int amount;
        if (ingredientSplit.length == 1) {
            amount = 1;
        } else {
            amount = Integer.parseInt(ingredientSplit[1]);
        }
        return getIngredient(ingredientSplit[0])
                .thenApplyAsync(ingredientOptional ->
                        ingredientOptional.map(ingredient -> new Pair<>(ingredient, amount))
                                .orElseThrow(() -> new IllegalArgumentException("Invalid ingredient string '" + ingredientStr + "' could not parse type"))
                );
    }


    @Override
    public CompletableFuture<Map<Ingredient, Integer>> getIngredientsWithAmount(List<String> stringList) throws
            IllegalArgumentException {
        if (stringList == null || stringList.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        Map<Ingredient, Integer> ingredientMap = new ConcurrentHashMap<>();
        CompletableFuture<?>[] ingredientsFuture = stringList.stream()
                .map(this::getIngredientWithAmount)
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
