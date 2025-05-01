package dev.jsinco.brewery.migrator.brew

import com.dre.brewery.BIngredients
import dev.jsinco.brewery.brew.Brew
import dev.jsinco.brewery.brew.BrewingStep
import dev.jsinco.brewery.breweries.CauldronType
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager
import dev.jsinco.brewery.ingredient.Ingredient
import dev.jsinco.brewery.moment.Moment
import dev.jsinco.brewery.moment.PassedMoment
import org.bukkit.inventory.ItemStack

object BrewMigration {

    fun migrateLegacyBrew(itemStack: ItemStack): Brew? {
        val legacyBrew = com.dre.brewery.Brew.get(itemStack)
        legacyBrew ?: let {
            return null
        }
        if (!legacyBrew.hasRecipe()) {
            return null
        }
        val currentRecipe = legacyBrew.currentRecipe
        val cookIngredients = legacyBrew.ingredients
        val cookTime = cookIngredients.cookedTime
        val age = legacyBrew.ageTime
        val barrel = legacyBrew.wood
        val distillRuns = legacyBrew.distillRuns
        val brewingSteps = mutableListOf<BrewingStep>()
        brewingSteps.add(
            BrewingStep.Cook(
                PassedMoment(cookTime.toLong()),
                convertIngredients(cookIngredients),
                CauldronType.WATER
            )
        )
        if (age > 0) {
            brewingSteps.add(BrewingStep.Age(PassedMoment((age * Moment.AGING_YEAR).toLong()), ))
        }
    }

    private fun convertIngredients(legacyIngredients: BIngredients): Map<Ingredient, Int> {
        val ingredientManager = BukkitIngredientManager.INSTANCE
        val output = mutableMapOf<Ingredient, Int>()
        for (legacyIngredient in legacyIngredients.ingredients) {
            val ingredient = ingredientManager.getIngredient(legacyIngredient)
        }
    }
}