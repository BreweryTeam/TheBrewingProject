package dev.jsinco.brewery.migrator.brew

import com.dre.brewery.BIngredients
import com.dre.brewery.recipe.PluginItem
import com.dre.brewery.recipe.SimpleItem
import dev.jsinco.brewery.brew.Brew
import dev.jsinco.brewery.brew.BrewImpl
import dev.jsinco.brewery.brew.BrewingStep
import dev.jsinco.brewery.breweries.BarrelType
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
        val cookIngredients = legacyBrew.ingredients
        val cookTime = cookIngredients.cookedTime
        val age = legacyBrew.ageTime
        val barrelType = legacyBrew.wood
        val distillRuns = legacyBrew.distillRuns
        val brewingSteps = mutableListOf<BrewingStep>()
        brewingSteps.add(
            BrewingStep.Cook(
                PassedMoment(cookTime.toLong()),
                convertIngredients(cookIngredients),
                CauldronType.WATER
            )
        )
        if (distillRuns > 0) {
            brewingSteps.add(BrewingStep.Distill(distillRuns.toInt()))
        }
        if (age > 0) {
            brewingSteps.add(
                BrewingStep.Age(
                    PassedMoment((age * Moment.AGING_YEAR).toLong()),
                    BarrelType.valueOf(barrelType.name)
                )
            )
        }
        return BrewImpl(brewingSteps)
    }

    private fun convertIngredients(legacyIngredients: BIngredients): Map<Ingredient, Int> {
        val ingredientManager = BukkitIngredientManager.INSTANCE
        val output = mutableMapOf<Ingredient, Int>()
        for (legacyIngredient in legacyIngredients.ingredients) {
            val id = if (legacyIngredient is SimpleItem) {
                legacyIngredient.material.key.toString()
            } else if (legacyIngredient is PluginItem) {
                legacyIngredient.plugin + ":" + legacyIngredient.itemId
            } else {
                // Custom items are not supported
                continue
            }
            ingredientManager.getIngredient(id).ifPresent {
                output[it] = legacyIngredient.amount
            }
        }
        return output
    }
}