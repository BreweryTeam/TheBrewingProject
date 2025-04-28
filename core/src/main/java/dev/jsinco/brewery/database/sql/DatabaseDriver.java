package dev.jsinco.brewery.database.sql;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public enum DatabaseDriver {
    SQLITE,
    MYSQL("mariadb"),
    POSTGRESQL("postgre");

    private List<String> alias;

    DatabaseDriver() {
        this.alias = List.of(this.name().toLowerCase(Locale.ROOT));
    }

    DatabaseDriver(String... alias) {
        this.alias = Streams.concat(Arrays.stream(alias), Stream.of(this.name().toLowerCase(Locale.ROOT))).toList();
    }

    public static @Nullable DatabaseDriver fromString(String databaseDriverString) {
        for (DatabaseDriver databaseDriver : DatabaseDriver.values()) {
            if (databaseDriver.alias.contains(databaseDriverString.toLowerCase(Locale.ROOT))) {
                return databaseDriver;
            }
        }
        return null;
    }
}
