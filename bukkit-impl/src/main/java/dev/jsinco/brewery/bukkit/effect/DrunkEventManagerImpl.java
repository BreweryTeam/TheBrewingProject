package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.api.effect.DrunkEventManager;
import dev.jsinco.brewery.bukkit.effect.event.DrunkEventExecutor;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public class DrunkEventManagerImpl implements DrunkEventManager {
    private final CustomEventRegistry customDrunkEventRegistry;
    private final DrunkEventExecutor drunkEventExecutor;

    public DrunkEventManagerImpl(CustomEventRegistry customDrunkEventRegistry, DrunkEventExecutor drunkEventExecutor) {
        this.customDrunkEventRegistry = customDrunkEventRegistry;
        this.drunkEventExecutor = drunkEventExecutor;
    }

    @Override
    public Collection<DrunkEvent> allEvents() {
        return Stream.concat(
                BreweryRegistry.DRUNK_EVENT.values().stream(),
                customDrunkEventRegistry.events().stream()
        ).toList();
    }

    @Override
    public void runEvent(UUID playerUuid, DrunkEvent event) {
        drunkEventExecutor.doDrunkEvent(playerUuid, event);
    }
}
