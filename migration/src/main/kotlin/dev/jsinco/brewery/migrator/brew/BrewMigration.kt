package dev.jsinco.brewery.migrator.brew

import com.dre.brewery.BIngredients
import com.dre.brewery.recipe.PluginItem
import com.dre.brewery.recipe.SimpleItem
import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewingStep
import dev.jsinco.brewery.api.breweries.BarrelType
import dev.jsinco.brewery.api.breweries.CauldronType
import dev.jsinco.brewery.api.ingredient.Ingredient
import dev.jsinco.brewery.api.moment.PassedMoment
import dev.jsinco.brewery.brew.AgeStepImpl
import dev.jsinco.brewery.brew.BrewImpl
import dev.jsinco.brewery.brew.CookStepImpl
import dev.jsinco.brewery.brew.DistillStepImpl
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager
import dev.jsinco.brewery.configuration.Config
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
            CookStepImpl(
                PassedMoment(cookTime.toLong() * Config.config().cauldrons().cookingMinuteTicks()),
                convertIngredients(cookIngredients),
                CauldronType.WATER
            )
        )
        if (distillRuns > 0) {
            brewingSteps.add(DistillStepImpl(distillRuns.toInt()))
        }
        if (age > 0) {
            brewingSteps.add(
                AgeStepImpl(
                    PassedMoment((age * Config.config().barrels().agingYearTicks()).toLong()),
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
            ingredientManager.getIngredient(id).join().ifPresent {
                output[it] = legacyIngredient.amount
            }
        }
        return output
    }
}