package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.database.InsertableStoredData;
import dev.jsinco.brewery.database.RemovableStoredData;
import dev.jsinco.brewery.database.RetrievableStoredData;
import dev.jsinco.brewery.database.UpdateableStoredData;

import java.util.UUID;

public interface DrunkStateDataType<C> extends UpdateableStoredData<Pair<DrunkState, UUID>, C>, RemovableStoredData<UUID, C>,
        InsertableStoredData<Pair<DrunkState, UUID>, C>, RetrievableStoredData<Pair<DrunkState, UUID>, C> {
}
