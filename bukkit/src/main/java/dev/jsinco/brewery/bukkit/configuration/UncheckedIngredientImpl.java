package dev.jsinco.brewery.bukkit.configuration;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.configuration.UncheckedIngredient;
import net.kyori.adventure.key.Key;

import java.util.Objects;
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
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UncheckedIngredientImpl) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "UncheckedIngredient[" +
                "key=" + key + ']';
    }


}
