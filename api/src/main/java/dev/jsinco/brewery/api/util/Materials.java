package dev.jsinco.brewery.api.util;

import java.util.Set;

public sealed interface Materials {

    Set<Holder.Material> values();

    record TagBacked(BreweryKey key) implements Materials {

        @Override
        public Set<Holder.Material> values() {
            return HolderProviderHolder.instance().parseTag(key);
        }
    }

    record Singleton(Holder.Material backing) implements Materials {

        @Override
        public Set<Holder.Material> values() {
            return Set.of(backing);
        }
    }

    record SetBacked(Set<Holder.Material> values) implements Materials {

    }
}
