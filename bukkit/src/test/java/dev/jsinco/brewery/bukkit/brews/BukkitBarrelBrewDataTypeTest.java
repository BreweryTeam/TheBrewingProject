package dev.jsinco.brewery.bukkit.brews;

import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BukkitBarrelBrewDataType;
import dev.jsinco.brewery.bukkit.ingredient.SimpleIngredient;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.FileUtil;
import dev.jsinco.brewery.util.FutureUtil;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class BukkitBarrelBrewDataTypeTest {
    @MockBukkitInject
    ServerMock serverMock;

    private Database database;
    private World world;

    @BeforeEach
    void setUp() {
        TheBrewingProject theBrewingProject = MockBukkit.load(TheBrewingProject.class);
        this.database = theBrewingProject.getDatabase();
        this.world = serverMock.addSimpleWorld("hello_world!");
    }

    @Test
    void checkPersistence() throws SQLException, PersistenceException {
        prepareBarrel();
        BreweryLocation searchObject = new BreweryLocation(1, 2, 3, world.getUID());
        BrewImpl brew1 = new BrewImpl(
                List.of(
                        new CookStepImpl(new PassedMoment(10), Map.of(new SimpleIngredient(Material.ACACIA_BUTTON), 3), CauldronType.WATER),
                        new AgeStepImpl(new Interval(1010, 1010 + Moment.DEFAULT_AGING_YEAR), BarrelType.ACACIA)
                )
        );
        BrewImpl brew2 = new BrewImpl(
                List.of(
                        new CookStepImpl(new PassedMoment(10), Map.of(new SimpleIngredient(Material.ACACIA_BUTTON), 3), CauldronType.WATER),
                        new AgeStepImpl(new Interval(1010, 1010 + Moment.DEFAULT_AGING_YEAR), BarrelType.ACACIA)
                )
        );
        BukkitBarrelBrewDataType.BarrelContext barrelContext1 = new BukkitBarrelBrewDataType.BarrelContext(1, 2, 3, 0, world.getUID());
        BukkitBarrelBrewDataType.BarrelContext barrelContext2 = new BukkitBarrelBrewDataType.BarrelContext(1, 2, 3, 1, world.getUID());
        Pair<Brew, BukkitBarrelBrewDataType.BarrelContext> data1 = new Pair<>(brew1, barrelContext1);
        Pair<Brew, BukkitBarrelBrewDataType.BarrelContext> data2 = new Pair<>(brew2, barrelContext2);
        database.insertValue(BukkitBarrelBrewDataType.INSTANCE, data1);
        assertTrue(FutureUtil.mergeFutures(database.findNow(BukkitBarrelBrewDataType.INSTANCE, searchObject)).join().contains(new Pair<>(brew1, 0)));
        database.insertValue(BukkitBarrelBrewDataType.INSTANCE, data2);
        assertTrue(FutureUtil.mergeFutures(database.findNow(BukkitBarrelBrewDataType.INSTANCE, searchObject)).join().contains(new Pair<>(brew2, 1)));
        database.remove(BukkitBarrelBrewDataType.INSTANCE, data1);
        assertFalse(FutureUtil.mergeFutures(database.findNow(BukkitBarrelBrewDataType.INSTANCE, searchObject)).join().contains(new Pair<>(brew1, 0)));
    }

    private void prepareBarrel() throws SQLException {
        try (Connection connection = this.database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/database/generic/barrels/insert.sql"));
            preparedStatement.setInt(1, 0);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, 0);
            preparedStatement.setInt(4, 1);
            preparedStatement.setInt(5, 2);
            preparedStatement.setInt(6, 3);
            preparedStatement.setBytes(7, DecoderEncoder.asBytes(world.getUID()));
            preparedStatement.setString(8, "[1,2,3,4,5,6,7,8,9]");
            preparedStatement.setString(9, "test_format");
            preparedStatement.setString(10, BarrelType.ACACIA.key().toString());
            preparedStatement.setInt(11, 9);
            preparedStatement.execute();
        }
    }
}