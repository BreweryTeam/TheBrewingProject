package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.testutil.TBPServerMock;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DrunksManagerTest {

    private DrunksManagerImpl<Connection> drunksManager;
    private UUID playerUuid = UUID.randomUUID();
    private AtomicLong time = new AtomicLong();
    private DrunkenModifier alcohol;

    @BeforeEach
    void setup() {
        MockBukkit.mock(new TBPServerMock());
        Database database = MockBukkit.load(TheBrewingProject.class).getDatabase();
        this.drunksManager = new DrunksManagerImpl<>(new CustomEventRegistry(), BreweryRegistry.DRUNK_EVENT.values().stream()
                .map(NamedDrunkEvent::key)
                .collect(Collectors.toSet()),
                time::get,
                database,
                SqlDrunkStateDataType.INSTANCE,
                SqlDrunkenModifierDataType.INSTANCE
        );
        this.alcohol = DrunkenModifierSection.modifiers().modifier("alcohol");
    }
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void consume() {
        drunksManager.consume(playerUuid, List.of(new ModifierConsume(alcohol, 10D)), 0L);
        assertEquals(new DrunkStateImpl(0, -1, Map.of(alcohol, 10D)), drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, List.of(new ModifierConsume(alcohol, 0)), 400);
        assertEquals(new DrunkStateImpl(400, -1, Map.of(alcohol, 8D)), drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, List.of(new ModifierConsume(alcohol, -9)), 400);
        assertNull(drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, List.of(new ModifierConsume(alcohol, 0D)), -200);
        assertNull(drunksManager.getDrunkState(playerUuid));
    }

    @Test
    void consume_appliesEvent() {
        drunksManager.consume(playerUuid, List.of(new ModifierConsume(alcohol, 100)), 0);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (int i = 0; i < 100000; i++) {
            time.incrementAndGet();
            drunksManager.tick((uuid, event) -> {
                atomicBoolean.set(true);
            }, uuid -> true);
        }
        assertTrue(atomicBoolean.get());
    }

    @Test
    void consume_doesNotApplyEvent() {
        drunksManager.consume(playerUuid, new ModifierConsume(alcohol, 1));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (int i = 0; i < 100000; i++) {
            time.incrementAndGet();
            drunksManager.tick((uuid, event) -> {
                atomicBoolean.set(true);
            }, uuid -> true);
        }
        assertFalse(atomicBoolean.get());
    }

    @Test
    void clear() {
        drunksManager.consume(playerUuid, new ModifierConsume(alcohol, 100));
        drunksManager.clear(playerUuid);
        assertNull(drunksManager.getDrunkState(playerUuid));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (int i = 0; i < 100000; i++) {
            time.incrementAndGet();
            drunksManager.tick((uuid, event) -> {
                atomicBoolean.set(true);
            }, uuid -> true);
        }
        assertFalse(atomicBoolean.get());
    }

}