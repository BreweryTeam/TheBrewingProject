package dev.jsinco.brewery.database.session;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.database.Session;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DrunkenStateSession extends Session<DrunkenStateSession> {

    CompletableFuture<Void> insertModifier(DrunkenModifier drunkenModifier, double value, UUID playerUuid);

    CompletableFuture<Void> removeModifier(DrunkenModifier drunkenModifier, UUID playerUuid);

    CompletableFuture<List<ModifierLookupResult>> fetchDrunkenModifiers(UUID playerUuid);

    CompletableFuture<Void> insertState(DrunkState state, UUID playerUuid);

    CompletableFuture<Void> removeState(UUID playerUuid);

    CompletableFuture<Void> updateState(DrunkState state, UUID playerUuid);

    CompletableFuture<List<StateLookupResult>> retrieveAllStates();

    record ModifierLookupResult(DrunkenModifier modifier, double value) {
    }

    record StateLookupResult(DrunkState state, UUID playerUuid) {
    }
}
