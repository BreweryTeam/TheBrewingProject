package dev.jsinco.brewery.migrator.barrel

import com.dre.brewery.Barrel
import dev.jsinco.brewery.api.breweries.BarrelType
import dev.jsinco.brewery.api.structure.StructureMeta
import dev.jsinco.brewery.api.structure.StructureType
import dev.jsinco.brewery.api.util.Pair
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.api.BukkitAdapter
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrelDataType
import dev.jsinco.brewery.bukkit.structure.BarrelBlockDataMatcher
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure
import dev.jsinco.brewery.migrator.brew.BrewMigration
import org.bukkit.Location
import org.bukkit.World
import kotlin.jvm.optionals.getOrNull

object BarrelMigration {
    fun migrateWorld(world: World) {
        Barrel.getBarrels(world.uid)
            .asSequence()
            .mapNotNull { convertFormat(it) }
            .filter {
                TheBrewingProject.getInstance().placedStructureRegistry.getStructures(it.structure.positions())
                    .isEmpty()
            }
            .forEach {
                TheBrewingProject.getInstance().database.insertValue(BukkitBarrelDataType.INSTANCE, it)
            }
    }

    private fun convertFormat(legacyBarrel: Barrel): BukkitBarrel? {
        val bounds = legacyBarrel.bounds
        val intList = bounds.serializeToIntList()
        val x1 = intList[0]
        val y1 = intList[1]
        val z1 = intList[2]
        val barrelStructurePair = findBarrelStructure(
            Location(
                legacyBarrel.spigot.world, x1!!.toDouble(), y1!!.toDouble(),
                z1!!.toDouble()
            )
        )
        barrelStructurePair ?: let {
            return null
        }
        val structure = barrelStructurePair.first()
        val barrelType = barrelStructurePair.second()
        val bukkitBarrel = BukkitBarrel(
            BukkitAdapter.toLocation(structure.unique).get(),
            structure,
            structure.structure.getMeta(StructureMeta.INVENTORY_SIZE)!!,
            barrelType
        )
        structure.holder = bukkitBarrel
        legacyBarrel.inventory.contents.withIndex().asSequence()
            .forEach { legacyBrew ->
                val index = legacyBrew.index
                legacyBrew.value?.let {
                    bukkitBarrel.inventory.brews[index] = BrewMigration.migrateLegacyBrew(legacyBrew.value!!)
                }
            }
        return bukkitBarrel
    }

    private fun findBarrelStructure(
        pos: Location
    ): Pair<out PlacedBreweryStructure<BukkitBarrel>, BarrelType>? {
        return TheBrewingProject.getInstance().structureRegistry.getStructures(StructureType.BARREL)
            .stream()
            .map {
                PlacedBreweryStructure.findValid(
                    it,
                    pos,
                    BarrelBlockDataMatcher.INSTANCE,
                    BarrelType.PLACEABLE_TYPES
                )
            }
            .flatMap { it.stream() }
            .map { it as Pair<out PlacedBreweryStructure<BukkitBarrel>, BarrelType> }
            .findAny()
            .getOrNull()
    }
}