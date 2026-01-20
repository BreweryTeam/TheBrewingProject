package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface EventIntegration<E extends IntegrationEvent> extends Integration {

    Pattern EVENT_META_GREEDY_RE = Pattern.compile("([^{}]+)\\{(.*)}");

    Class<E> eClass();

    List<BreweryKey> listEventKeys();

    Optional<E> deserialize(SerializedEvent serializedEvent);

    SerializedEvent serialize(E event);

    record SerializedEvent(BreweryKey key, @Nullable String meta) {
    }

    static SerializedEvent parseEvent(String string){
        BreweryKey key;
        String meta;
        Matcher matcher = EVENT_META_GREEDY_RE.matcher(string);
        if (matcher.matches()) {
            String group2 = matcher.group(2);
            meta = group2.isBlank() ? null : group2;
            key = BreweryKey.parse(matcher.group(1));
        } else {
            key = BreweryKey.parse(string);
            meta = null;
        }
        return new EventIntegration.SerializedEvent(key, meta);
    }
}
