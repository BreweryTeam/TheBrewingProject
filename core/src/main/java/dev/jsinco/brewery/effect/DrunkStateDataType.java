package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.database.InsertableStoredData;
import dev.jsinco.brewery.database.RemovableStoredData;
import dev.jsinco.brewery.database.RetrievableStoredData;
import dev.jsinco.brewery.database.UpdateableStoredData;
import dev.jsinco.brewery.api.util.Pair;

import java.util.UUID;

public interface DrunkStateDataType<C> extends UpdateableStoredData<Pair<DrunkStateImpl, UUID>, C>, RemovableStoredData<UUID, C>,
        InsertableStoredData<Pair<DrunkStateImpl, UUID>, C>, RetrievableStoredData<Pair<DrunkStateImpl, UUID>, C> {
}
