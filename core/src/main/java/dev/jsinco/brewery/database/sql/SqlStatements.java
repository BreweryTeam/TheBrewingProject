package dev.jsinco.brewery.database.sql;

import dev.jsinco.brewery.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlStatements {

    private final String folder;
    private final Map<Type, String> statements = new ConcurrentHashMap<>();

    public SqlStatements(String folder) {
        this.folder = folder;
        init();
    }

    public void init() {
        for (Type type : Type.values()) {
            try (InputStream inputStream = SqlStatements.class.getResourceAsStream(type.path(folder))) {
                if (inputStream == null) {
                    continue;
                }
                String statement = new String(inputStream.readAllBytes());
                statements.put(type, statement);
            } catch (IOException e) {
                Logger.logErr(e);
            }
        }
    }

    public @NotNull String get(Type type) {
        if (!statements.containsKey(type)) {
            throw new IllegalArgumentException("Statement does not exists.");
        }
        return statements.get(type);
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
