package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.StringUtil;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface EventIntegration<E extends IntegrationEvent> extends Integration {

    Pattern EVENT_META_GREEDY_RE = Pattern.compile("([^{}]+)\\{(.*)}");

    Class<E> eClass();

    List<BreweryKey> listEventKeys();

    Optional<E> convertToEvent(EventData eventData);

    EventData ConvertToData(E event);

    final class EventData {
        private final BreweryKey key;
        private final MetaData metaData;

        public EventData(BreweryKey key) {
            this.key = key;
            this.metaData = new MetaData();
        }

        private EventData(BreweryKey key, MetaData metaData) {
            this.key = key;
            this.metaData = metaData;
        }

        public BreweryKey key() {
            return key;
        }

        public <T> @Nullable T data(Key key, MetaDataType<String, T> type) {
            return metaData.meta(key, type);
        }

        public <T> EventData withData(Key key, MetaDataType<String, T> type, T value) {
            return new EventData(this.key,
                    metaData.withMeta(key, type, value)
            );
        }

        public Set<Key> dataKeys() {
            return metaData.metaKeys();
        }

    }

    static EventData parseEvent(String string) {
        BreweryKey key;
        String metaString;
        Matcher matcher = EVENT_META_GREEDY_RE.matcher(string);
        if (matcher.matches()) {
            String group2 = matcher.group(2);
            metaString = group2.isBlank() ? null : group2;
            key = BreweryKey.parse(matcher.group(1));
        } else {
            key = BreweryKey.parse(string);
            metaString = null;
        }
        if (metaString == null) {
            return new EventData(key);
        }
        return new EventData(key, StringUtil.parseMeta(metaString));
    }
}
