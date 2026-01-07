package dev.jsinco.brewery.api.ingredient;

import java.util.List;

public interface ComplexIngredient extends Ingredient {

    /**
     * @return A non-empty list with the complex ingredients derivatives
     */
    List<BaseIngredient> derivatives();
}
