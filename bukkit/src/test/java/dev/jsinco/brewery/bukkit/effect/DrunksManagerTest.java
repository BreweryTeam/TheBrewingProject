package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.BreweryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class DrunksManagerTest {

    private DrunksManagerImpl<Connection> drunksManager;
    private UUID playerUuid = UUID.randomUUID();
    private AtomicLong time = new AtomicLong();

    @BeforeEach
    void setup() {
        this.drunksManager = new DrunksManagerImpl<>(new CustomEventRegistry(), BreweryRegistry.DRUNK_EVENT.values().stream()
                .map(NamedDrunkEvent::key)
                .collect(Collectors.toSet()),
                time::get,
                MockBukkit.load(TheBrewingProject.class).getDatabase(),
                SqlDrunkStateDataType.INSTANCE
        );
    }

    @Test
    void consume() {
        drunksManager.consume(playerUuid, 10, 0, 0);
        assertEquals(new DrunkStateImpl(10, 0, 0, -1), drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, 0, 0, 400);
        assertEquals(new DrunkStateImpl(8, 0, 400, -1), drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, -9, 0, 0);
        assertNull(drunksManager.getDrunkState(playerUuid));
        drunksManager.consume(playerUuid, -10, 20, 0);
        assertNull(drunksManager.getDrunkState(playerUuid));
    }

    @Test
    void consume_negativeValued() {
        drunksManager.consume(playerUuid, -10, 20);
        assertNull(drunksManager.getDrunkState(playerUuid));
    }

    @Test
    void consume_appliesEvent() {
        drunksManager.consume(playerUuid, 100, 0);
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
        drunksManager.consume(playerUuid, 1, 0);
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
        drunksManager.consume(playerUuid, 100, 0);
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