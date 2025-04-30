package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.ingredient.ScoredIngredient;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public sealed interface BrewingStep {

    /**
     * Calculate how close this brewing step is to another brewing step.
     *
     * @param other The other step to compare to
     * @return A proximity index in the range [0, 1], where 1 is 100% match
     */
    double proximity(BrewingStep other);

    StepType stepType();

    private static double nearbyValueScore(long expected, long value) {
        double diff = Math.abs(expected - value);
        return 1 - Math.max(diff / expected, 0D);
    }

    private static double getIngredientsScore(Map<Ingredient, Integer> target, Map<Ingredient, Integer> actual) {
        List<Pair<Double, Integer>> customScores = actual.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof ScoredIngredient)
                .map(entry -> new Pair<>(((ScoredIngredient) entry.getKey()).score(), entry.getValue()))
                .toList();
        Pair<Double, Integer> scoredIngredientPair = customScores.stream().reduce(new Pair<>(1D, 1),
                (pair1, pair2) -> new Pair<>(pair1.first() * Math.pow(pair2.first(), pair2.second()), pair1.second() + pair2.second()));
        // Average out t
        double output = Math.pow(scoredIngredientPair.first(), (double) 1 / scoredIngredientPair.second());
        Map<Ingredient, Integer> modifiedTarget = compressIngredients(target);
        Map<Ingredient, Integer> modifiedActual = compressIngredients(actual);

        if (modifiedTarget.size() != modifiedActual.size()) {
            return 0;
        }
        for (Map.Entry<Ingredient, Integer> targetEntry : modifiedTarget.entrySet()) {
            Integer actualAmount = modifiedActual.get(targetEntry.getKey());
            if (actualAmount == null || actualAmount == 0) {
                return 0;
            }
            output *= nearbyValueScore(targetEntry.getValue(), actualAmount);
        }
        return output;
    }

    private static Map<Ingredient, Integer> compressIngredients(Map<Ingredient, Integer> ingredients) {
        Map<Ingredient, Integer> output = new HashMap<>();
        ingredients.entrySet()
                .stream()
                .map(entry -> {
                    if (entry.getKey() instanceof ScoredIngredient scoredIngredient) {
                        return new Pair<>(scoredIngredient.baseIngredient(), entry.getValue());
                    } else {
                        return new Pair<>(entry.getKey(), entry.getValue());
                    }
                })
                .forEach(pair -> IngredientManager.insertIngredientIntoMap(output, pair));
        return output;
    }

    double maximumScore(BrewingStep other);

    record Cook(Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                CauldronType cauldronType) implements BrewingStep {

        public Cook withBrewTime(Moment brewTime) {
            return new Cook(brewTime, this.ingredients, this.cauldronType);
        }

        public Cook withIngredients(Map<Ingredient, Integer> ingredients) {
            return new Cook(this.brewTime, ingredients, this.cauldronType);
        }

        @Override
        public double proximity(BrewingStep other) {
            if (!(other instanceof Cook(
                    Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients, CauldronType otherType
            ))) {
                return 0D;
            }
            double cauldronTypeScore = cauldronType.equals(otherType) ? 1D : 0D;
            return cauldronTypeScore * Math.sqrt(BrewingStep.nearbyValueScore(this.brewTime.moment(), otherTime.moment())) * BrewingStep.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        }

        @Override
        public StepType stepType() {
            return StepType.COOK;
        }

        @Override
        public double maximumScore(BrewingStep other) {
            if (!(other instanceof Cook cook)) {
                return 0D;
            }
            double maximumCookTimeScore = cauldronType.equals(cook.cauldronType) && this.brewTime.moment() > cook.brewTime.moment() ? 1D : BrewingStep.nearbyValueScore(this.brewTime.moment(), cook.brewTime.moment());
            return BrewingStep.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) cook.ingredients) * maximumCookTimeScore;
        }
    }

    record Distill(int runs) implements BrewingStep {

        public Distill incrementAmount() {
            return new Distill(this.runs + 1);
        }

        @Override
        public double proximity(BrewingStep other) {
            if (!(other instanceof Distill(int otherRuns))) {
                return 0D;
            }
            return Math.sqrt(BrewingStep.nearbyValueScore(this.runs, otherRuns));
        }

        @Override
        public StepType stepType() {
            return StepType.DISTILL;
        }

        @Override
        public double maximumScore(BrewingStep other) {
            if (!(other instanceof Distill(int runs1))) {
                return 0D;
            }
            return runs1 < this.runs ? 1D : BrewingStep.nearbyValueScore(this.runs, runs1);
        }
    }

    record Age(Moment age, BarrelType barrelType) implements BrewingStep {

        public Age withAge(Moment age) {
            return new Age(age, this.barrelType);
        }

        @Override
        public double proximity(BrewingStep other) {
            if (!(other instanceof Age(Moment otherAge, BarrelType otherType))) {
                return 0D;
            }
            double barrelTypeScore = barrelType.equals(BarrelType.ANY) || barrelType.equals(otherType) ? 1D : 0.9D;
            return barrelTypeScore * Math.sqrt(BrewingStep.nearbyValueScore(this.age.moment(), otherAge.moment()));
        }

        @Override
        public StepType stepType() {
            return StepType.AGE;
        }

        @Override
        public double maximumScore(BrewingStep other) {
            if (!(other instanceof Age age)) {
                return 0D;
            }
            return age.age.moment() < this.age.moment() ? 1D : BrewingStep.nearbyValueScore(this.age.moment(), age.age.moment());
        }
    }

    record Mix(Moment time, Map<? extends Ingredient, Integer> ingredients) implements BrewingStep {
        public Mix withIngredients(Map<Ingredient, Integer> ingredients) {
            return new Mix(this.time, ingredients);
        }

        @Override
        public double proximity(BrewingStep other) {
            if (!(other instanceof Mix(Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients))) {
                return 0D;
            }
            return BrewingStep.nearbyValueScore(this.time.moment(), otherTime.moment()) * BrewingStep.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        }

        @Override
        public StepType stepType() {
            return StepType.MIX;
        }

        @Override
        public double maximumScore(BrewingStep other) {
            if (!(other instanceof Mix(Moment time1, Map<? extends Ingredient, Integer> ingredients1))) {
                return 0D;
            }
            double mixTimeScore = time1.moment() < this.time.moment() ? 1D : BrewingStep.nearbyValueScore(this.time.moment(), time1.moment());
            return BrewingStep.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) ingredients1) * Math.sqrt(mixTimeScore);
        }

        public Mix withTime(Moment time) {
            return new Mix(time, this.ingredients);
        }
    }

    enum StepType {
        COOK,
        DISTILL,
        AGE,
        MIX
    }

}
