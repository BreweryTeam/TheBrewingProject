package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.ScoredIngredient;
import dev.jsinco.brewery.util.BreweryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ScoredBreweryIngredient implements ScoredIngredient {

    private final double score;
    private final BreweryKey ingredientKey;
    private final String displayName;

    public ScoredBreweryIngredient(BreweryKey ingredientKey, double score, String displayName) {
        this.ingredientKey = ingredientKey;
        this.score = score;
        this.displayName = displayName;
    }

    @Override
    public @NotNull String getKey() {
        return ingredientKey.toString();
    }

    @Override
    public @NotNull String displayName() {
        return displayName + "-" + score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScoredBreweryIngredient that = (ScoredBreweryIngredient) o;
        return Objects.equals(ingredientKey, that.ingredientKey) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientKey, score);
    }

    @Override
    public double score() {
        return score;
    }

    @Override
    public Ingredient baseIngredient() {
        return new BreweryIngredient(ingredientKey, displayName);
    }
}
