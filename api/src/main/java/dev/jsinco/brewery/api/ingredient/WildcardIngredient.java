package dev.jsinco.brewery.api.ingredient;

public interface WildcardIngredient extends IngredientInput {

    static WildcardIngredient get(String value) {
        return IngredientInputProviderHolder.instance()
                .wildcard(value);
    }
}
