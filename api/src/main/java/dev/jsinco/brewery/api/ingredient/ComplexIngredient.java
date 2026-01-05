package dev.jsinco.brewery.api.ingredient;

import java.util.List;

public interface ComplexIngredient extends Ingredient {

    List<BaseIngredient> derivatives();
}
