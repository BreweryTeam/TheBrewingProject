package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.database.FindableStoredData;
import dev.jsinco.brewery.database.InsertableStoredData;
import dev.jsinco.brewery.database.RemovableStoredData;

import java.util.UUID;

public interface DrunkenModifierDataType<C> extends InsertableStoredData<Pair<DrunkenModifierDataType.Data, Double>, C>,
        RemovableStoredData<DrunkenModifierDataType.Data, C>, FindableStoredData<Pair<DrunkenModifier, Double>, UUID, C> {

    record Data(DrunkenModifier modifier, UUID playerUuid) {
    }
}
