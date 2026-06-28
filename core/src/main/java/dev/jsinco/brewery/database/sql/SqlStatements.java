package dev.jsinco.brewery.database.sql;

import dev.jsinco.brewery.api.util.Logger;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SqlStatements {

    private final String folder;
    private final Map<String, String> statements = new ConcurrentHashMap<>();

    public SqlStatements(String folder) {
        this.folder = folder;
        init();
    }

    public void init() {
        for (Type type : Type.values()) {
            read(type.path(folder))
                    .ifPresent(statement ->
                            statements.put(type.name().toLowerCase(Locale.ROOT), statement)
                    );
        }
    }

    private Optional<String> read(String path) {
        try (InputStream inputStream = SqlStatements.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                return Optional.empty();
            }
            return Optional.of(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            Logger.logErr(e);
        }
        return Optional.empty();
    }

    public @NonNull String get(Type type) {
        if (!statements.containsKey(type.name().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Statement does not exist");
        }
        return statements.get(type.name().toLowerCase(Locale.ROOT));
    }

    public @NonNull String get(String type) {
        String lowercaseType = type.toLowerCase(Locale.ROOT);
        if (!statements.containsKey(lowercaseType)) {
            String statement = read(compilePath(folder, lowercaseType))
                    .orElseThrow(() -> new IllegalArgumentException("Statement does not exist."));
            statements.put(lowercaseType, statement);
            return statement;
        }
        return statements.get(lowercaseType);
    }

    private static String compilePath(String root, String type) {
        return root + "/" + type.toLowerCase(Locale.ROOT) + ".sql";
    }

    public enum Type {
        SELECT_ALL,
        DELETE,
        UPDATE,
        INSERT,
        FIND,
        GET_SINGLETON,
        SET_SINGLETON;

        public String path(String directoryPath) {
            return directoryPath + "/" + name().toLowerCase(Locale.ROOT) + ".sql";
        }
    }
}
