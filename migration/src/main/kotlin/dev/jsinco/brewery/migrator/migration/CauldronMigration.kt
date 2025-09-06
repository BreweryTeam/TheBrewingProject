package dev.jsinco.brewery.migrator.migration

import com.dre.brewery.BCauldron
import dev.jsinco.brewery.api.breweries.CauldronType
import dev.jsinco.brewery.api.moment.Interval
import dev.jsinco.brewery.brew.BrewImpl
import dev.jsinco.brewery.brew.CookStepImpl
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.api.BukkitAdapter
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType
import org.bukkit.World

object CauldronMigration {

    fun migrateWorld(world: World) {
        BCauldron.bcauldrons.values.asSequence()
            .filter { it.block.world == world }
            .mapNotNull { convertToTbpCauldron(it) }
            .forEach {
                TheBrewingProject.getInstance().database.insertValue(BukkitCauldronDataType.INSTANCE, it)
                TheBrewingProject.getInstance().breweryRegistry.addActiveSinglePositionStructure(it)
            }
    }

    fun convertToTbpCauldron(bCauldron: BCauldron): BukkitCauldron? {
        val ingredients = BrewMigration.convertIngredients(bCauldron.ingredients)
        val tbpTime = TheBrewingProject.getInstance().time
        return BukkitCauldron(
            BrewImpl(
                CookStepImpl(
                    Interval(tbpTime - bCauldron.state, tbpTime),
                    ingredients,
                    CauldronType.WATER
                )
            ),
            BukkitAdapter.toBreweryLocation(bCauldron.block)
        )
    }
}