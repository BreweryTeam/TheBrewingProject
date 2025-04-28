package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrelDataType;
import dev.jsinco.brewery.bukkit.structure.BarrelBlockDataMatcher;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.structure.StructurePlacerUtils;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.structure.StructureType;
import dev.jsinco.brewery.util.Pair;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class BarrelDataTypeTest {
    @MockBukkitInject
    ServerMock server;
    private @NotNull WorldMock world;
    private Database database;

    @BeforeEach
    void setUp() {
        this.world = server.addSimpleWorld("hello!");
        TheBrewingProject theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        this.database = theBrewingProject.getDatabase();
    }

    @Test
    void checkPersistence() throws PersistenceException {
        StructurePlacerUtils.constructSmallOakBarrel(world);
        Location barrelBlock = new Location(world, -3, 1, 2);
        Optional<Pair<PlacedBreweryStructure<BukkitBarrel>, BarrelType>> breweryStructureOptional = TheBrewingProject.getInstance()
                .getStructureRegistry()
                .getPossibleStructures(barrelBlock.getBlock().getType(), StructureType.BARREL)
                .stream()
                .map(breweryStructure ->
                        PlacedBreweryStructure.<BarrelType, BukkitBarrel>findValid(breweryStructure, barrelBlock, BarrelBlockDataMatcher.INSTANCE, BarrelType.PLACEABLE_TYPES)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        BukkitBarrel barrel = new BukkitBarrel(new Location(world, 1, 2, 3), breweryStructureOptional.get().first(), 9, BarrelType.OAK);
        BrewInventory inventory = barrel.getInventory();
        inventory.set(new BrewImpl(
                List.of(
                        new BrewingStep.Cook(new PassedMoment(10), Map.of(), CauldronType.WATER),
                        new BrewingStep.Age(new Interval(10, 10), BarrelType.OAK)
                )
        ), 4);
        inventory.set(
                new BrewImpl(
                        List.of(
                                new BrewingStep.Cook(new PassedMoment(10), Map.of(), CauldronType.WATER),
                                new BrewingStep.Age(new Interval(10, 10), BarrelType.OAK)
                        )
                ), 5
        );
        database.insertValue(BukkitBarrelDataType.INSTANCE, barrel);
        database.flush().join();
        List<BukkitBarrel> retrievedBarrels = database.findNow(BukkitBarrelDataType.INSTANCE, world.getUID());
        assertEquals(1, retrievedBarrels.size());
        BukkitBarrel retrievedBarrel = retrievedBarrels.get(0);
        assertEquals(2, retrievedBarrel.getBrews().size());
        database.remove(BukkitBarrelDataType.INSTANCE, barrel);
        assertTrue(database.findNow(BukkitBarrelDataType.INSTANCE, world.getUID()).isEmpty());
    }

}