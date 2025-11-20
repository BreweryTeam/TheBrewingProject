package dev.jsinco.brewery.api.util;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface HolderProvider {

    Optional<Holder.Material> material(BreweryKey materialString);

    Optional<Holder.Player> player(UUID playerUuid);

    Optional<Holder.World> world(UUID worldUuid);

    Set<Holder.Material> parseTag(BreweryKey tagString);
}
