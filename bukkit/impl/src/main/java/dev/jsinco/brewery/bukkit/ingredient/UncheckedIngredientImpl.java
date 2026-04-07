package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientInput;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import dev.jsinco.brewery.api.ingredient.WildcardIngredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import net.kyori.adventure.key.Key;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class UncheckedIngredientImpl implements UncheckedIngredient {
    private CompletableFuture<Optional<Ingredient>> value;
    private final BreweryKey key;

    public UncheckedIngredientImpl(BreweryKey key) {
        this.value = BukkitIngredientManager.INSTANCE.getIngredient(key.toString());
        this.key = key;
    }

    public UncheckedIngredientImpl(Ingredient ingredient) {
        this.value = CompletableFuture.completedFuture(Optional.of(ingredient));
        this.key = BreweryKey.parse(ingredient.getKey(), Key.MINECRAFT_NAMESPACE);
    }

    public Optional<Ingredient> retrieve() {
        if (value == null) {
            return Optional.empty();
        }
        if (value.isCancelled()) {
            value = null;
            Logger.logErr("Ingredient future cancelled, key was: " + key);
            return Optional.empty();
        }
        if (value.isCompletedExceptionally()) {
            Logger.logErr(value.exceptionNow());
            value = null;
            return Optional.empty();
        }
        if (!value.isDone()) {
            return Optional.empty();
        }
        return value.join();
    }

    public BreweryKey key() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UncheckedIngredient otherUnchecked)) {
            return false;
        }
        return otherUnchecked.key().equals(key());
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "UncheckedIngredient[" +
                "key=" + key + ']';
    }


    @Override
    public boolean matches(IngredientInput other) {
        if (other instanceof WildcardIngredient) {
            return other.matches(this);
        }
        return other.equals(this);
    }
}
