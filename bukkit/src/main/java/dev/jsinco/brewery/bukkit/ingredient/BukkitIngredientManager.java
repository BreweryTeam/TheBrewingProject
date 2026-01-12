package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.integration.IntegrationManagerImpl;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
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
