package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.KeyUtil;
import dev.jsinco.brewery.api.util.StringUtil;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class EventData {
    private static final Pattern EVENT_META_GREEDY_RE = Pattern.compile("([^{}]+)\\{(.*)}");
    public static final Key DURATION_KEY = KeyUtil.brewery("duration");
    /**
     * Can be used to override the probability for this event
     */
    public static final Key PROBABILITY_KEY = KeyUtil.brewery("probability");

    private final BreweryKey key;
    private final MetaData metaData;

    public EventData(BreweryKey key) {
        this.key = key;
        this.metaData = new MetaData();
    }

    EventData(BreweryKey key, MetaData metaData) {
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

    public String serialized() {
        Set<Key> keys = dataKeys();
        if(keys.isEmpty()) {
            return key.minimalized();
        }
       return key().minimalized() + "{" + keys.stream().map(key -> KeyUtil.minimalize(key) + "=" + data(key, MetaDataType.STRING)).collect(Collectors.joining(",")) +"}";
    }

    public static EventData deserialize(String string) {
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
