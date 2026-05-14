package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.IngredientMeta;
import dev.jsinco.brewery.api.ingredient.IngredientWithMeta;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.brew.BrewImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class BrewUtil {

    private BrewUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<BaseIngredient, Integer> sanitizeIngredients(Map<? extends Ingredient, Integer> ingredients) {
        Map<BaseIngredient, Integer> output = new HashMap<>();
        for (Map.Entry<? extends Ingredient, Integer> entry : ingredients.entrySet()) {
            Ingredient ingredient = entry.getKey();
            if (ingredient instanceof IngredientGroup ingredientGroup) {
                ingredient = ingredientGroup.alternatives().stream().max(Comparator.comparing(groupIngredient ->
                        BrewUtil.score(groupIngredient).orElse(1D)
                )).orElse(null);
            }
            if (ingredient == null) {
                continue;
            }
            output.put(ingredient.toBaseIngredient(), entry.getValue());
        }
        return output;
    }

    public static List<BrewingStep> sanitizeSteps(List<BrewingStep> steps) {
        return steps.stream()
                .map(BrewUtil::sanitizeStep)
                .toList();
    }

    public static BrewingStep sanitizeStep(BrewingStep step) {
        return switch (step) {
            case BrewingStep.Mix mix -> mix.withIngredients(sanitizeIngredients(mix.ingredients()));
            case BrewingStep.Cook cook -> cook.withIngredients(sanitizeIngredients(cook.ingredients()));
            default -> step;
        };
    }

    public static Optional<Double> score(Ingredient ingredient) {
        if (ingredient instanceof IngredientWithMeta ingredientWithMeta) {
            return Optional.ofNullable(ingredientWithMeta.get(IngredientMeta.SCORE));
        }
        return Optional.empty();
    }

    public static List<BaseIngredient> getRecipeIngredients(Recipe<?> recipe) {
        return getRecipeIngredients(recipe.getSteps());
    }

    public static List<BaseIngredient> getRecipeIngredients(List<BrewingStep> steps) {
        return steps
                .stream()
                .filter(BrewingStep.IngredientsStep.class::isInstance)
                .map(BrewingStep.IngredientsStep.class::cast)
                .map(BrewingStep.IngredientsStep::ingredients)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .flatMap(ingredient -> {
                    if (ingredient instanceof IngredientGroup ingredientGroup) {
                        return ingredientGroup.alternatives().stream()
                                .map(Ingredient::toBaseIngredient);
                    }
                    return Stream.of(ingredient.toBaseIngredient());
                })
                .toList();
    }

    public static List<List<BrewingStep>> variations(List<BrewingStep> steps, RecipeRegistry<?> registry) {
        List<List<BrewingStep>> sections = new ArrayList<>();
        List<BrewingStep> latest = new ArrayList<>();
        for (BrewingStep step : steps) {
            if (!latest.isEmpty() || step instanceof BrewingStep.IngredientsStep) {
                sections.add(latest);
                latest = new ArrayList<>();
            }
            latest.add(step);
        }
        if (!latest.isEmpty()) {
            sections.add(latest);
        }
        if (sections.size() < 2) {
            return sections;
        }
        return variations0(sections, registry, null);
    }

    private static List<List<BrewingStep>> variations0(List<List<BrewingStep>> sections, RecipeRegistry<?> registry, @Nullable Ingredient carryoverIngredient) {
        if (sections.isEmpty()) {
            return List.of();
        }
        List<BrewingStep> built = new ArrayList<>(sections.getFirst());
        if (built.isEmpty()) {
            return List.of();
        }
        BrewingStep firstStep = built.getFirst();
        if (carryoverIngredient != null) {
            if (firstStep instanceof BrewingStep.IngredientsStep ingredientsStep) {
                Map<Ingredient, Integer> ingredients = new HashMap<>(ingredientsStep.ingredients());
                ingredients.put(carryoverIngredient, 3);
                built.set(0, ingredientsStep.withIngredients(
                        ingredients
                ));
            } else {
                // Avoid generating invalid variations
                return List.of();
            }
        }
        if (sections.size() == 1) {
            return List.of(built);
        }

        List<List<BrewingStep>> remaining = sections.subList(1, sections.size());
        List<List<BrewingStep>> output = new ArrayList<>();
        for (
                int i = 0; i < remaining.size(); i++) {
            List<BrewingStep> addition = remaining.get(i);
            built.addAll(addition);
            if (i + 1 >= remaining.size()) {
                continue;
            }
            Brew brew = new BrewImpl(built);
            final int iFinal = i;
            registry.possibleRecipes(built)
                    .stream()
                    .flatMap(recipe -> {
                        BrewScore score = brew.score(recipe);
                        double scoreValue = score.score();
                        return (score.completed() && scoreValue > 0) ? Stream.of(recipe.toIngredient(scoreValue)) : Stream.empty();
                    })
                    .map(ingredient -> variations0(remaining.subList(iFinal, remaining.size()), registry, ingredient))
                    .forEach(output::addAll);
        }
        output.add(built);
        return output;
    }

    public static Map<Ingredient, Integer> averageIngredients(Map<? extends Ingredient, Integer> a, Map<? extends Ingredient, Integer> b) {
        Map<Ingredient, Integer> mapA = (Map<Ingredient, Integer>) a;
        Map<Ingredient, Integer> mapB = (Map<Ingredient, Integer>) b;
        Set<Ingredient> allKeys = new HashSet<>(mapA.keySet());
        allKeys.addAll(mapB.keySet());
        Map<Ingredient, Integer> result = new HashMap<>();
        for (Ingredient key : allKeys) {
            int countA = mapA.getOrDefault(key, 0);
            int countB = mapB.getOrDefault(key, 0);
            int avg = (countA + countB + 1) / 2;
            if (avg > 0) result.put(key, avg);
        }
        return result;
    }


    public static Optional<Brew> mergeBrews(Brew existing, Brew added) {
        List<BrewingStep> existingSteps = existing.getCompletedSteps();
        List<BrewingStep> addedSteps = added.getCompletedSteps();
        int mergeCount = Math.min(existingSteps.size(), addedSteps.size());
        List<BrewingStep> mergedSteps = new ArrayList<>(existingSteps.size());
        for (int i = 0; i < mergeCount; i++) {
            Optional<BrewingStep> optionalBrewingStep = existingSteps.get(i).merge(addedSteps.get(i));
            if (optionalBrewingStep.isEmpty()) {
                return Optional.empty();
            }
            optionalBrewingStep.ifPresent(mergedSteps::add);
        }
        for (int i = mergeCount; i < existingSteps.size(); i++) {
            mergedSteps.add(existingSteps.get(i));
        }
        return Optional.of(existing.withStepsReplaced(mergedSteps));
    }
}
