package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BreweryTimeDataTypeTest {

    Database database;

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
        this.database = MockBukkit.load(TheBrewingProject.class).getDatabase();
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void checkPersistence() throws PersistenceException {
        this.database.setSingleton(BreweryTimeDataType.INSTANCE, 10L);
        assertEquals(10L, this.database.getSingletonNow(BreweryTimeDataType.INSTANCE));
        this.database.setSingleton(BreweryTimeDataType.INSTANCE, 20L);
        assertEquals(20L, this.database.getSingletonNow(BreweryTimeDataType.INSTANCE));
    }
}