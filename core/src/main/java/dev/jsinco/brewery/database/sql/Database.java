package dev.jsinco.brewery.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jsinco.brewery.database.FindableStoredData;
import dev.jsinco.brewery.database.InsertableStoredData;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.database.RemovableStoredData;
import dev.jsinco.brewery.database.RetrievableStoredData;
import dev.jsinco.brewery.database.SingletonStoredData;
import dev.jsinco.brewery.database.UpdateableStoredData;
import dev.jsinco.brewery.util.FileUtil;
import dev.jsinco.brewery.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Database implements PersistenceHandler<Connection> {

    private static final int BREWERY_DATABASE_VERSION = 2;
    private final DatabaseDriver driver;
    private HikariDataSource hikariDataSource;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Database(DatabaseDriver databaseDriver) {
        this.driver = databaseDriver;
    }

    public void init(File dataFolder) throws IOException, SQLException {
        HikariConfig config = switch (driver) {
            case SQLITE -> getHikariConfigForSqlite(dataFolder);
            default -> throw new UnsupportedOperationException("Currently not implemented");
        };
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");
        this.hikariDataSource = new HikariDataSource(config);
        try (Connection connection = hikariDataSource.getConnection()) {
            createTables(connection);
        }
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    private static @NotNull HikariConfig getHikariConfigForSqlite(File dataFolder) throws IOException {
        File databaseFile = new File(dataFolder, "brewery.db");
        if (!databaseFile.exists() && !databaseFile.getParentFile().mkdirs() && !databaseFile.createNewFile()) {
            throw new IOException("Could not create file or dirs");
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SQLiteConnectionPool");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile);
        return hikariConfig;
    }

    private void createTables(Connection connection) throws SQLException {
        for (String statement : FileUtil.readInternalResource("/database/" + driver.name().toLowerCase(Locale.ROOT) + "/create_all_tables.sql").split(";")) {
            connection.prepareStatement(statement + ";").execute();
        }
        ResultSet resultSet = connection.prepareStatement(FileUtil.readInternalResource("/database/generic/get_version.sql")).executeQuery();
        if (resultSet.next()) {
            int previousVersion = resultSet.getInt("version");
            resultSet.close();
            if (previousVersion < BREWERY_DATABASE_VERSION) {
                for (int i = previousVersion; i < BREWERY_DATABASE_VERSION; i++) {
                    runMigration(i, connection);
                }
            } else if (previousVersion > BREWERY_DATABASE_VERSION) {
                throw new IllegalStateException("Can not downgrade The Brewing Project!");
            }
        }
        PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/database/generic/set_version.sql"));
        preparedStatement.setInt(1, BREWERY_DATABASE_VERSION);
        preparedStatement.execute();
    }

    private void runMigration(int version, Connection connection) throws SQLException {
        switch (version) {
            case 0 -> {
                for (String statement : FileUtil.readInternalResource("/database/migration/version_migration.sql").split(";")) {
                    connection.prepareStatement(statement + ";").execute();
                }
            }
            case 1 -> {
                for (String statement : FileUtil.readInternalResource("/database/migration/foreign_keys_on_migration.sql").split(";")) {
                    connection.prepareStatement(statement + ";").execute();
                }
            }
            default -> throw new IllegalStateException("Unimplemented migration from version: " + version);
        }
    }

    @Override
    public <T> CompletableFuture<List<T>> retrieveAll(RetrievableStoredData<T, Connection> dataType) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                future.complete(dataType.retrieveAll(connection));
            } catch (SQLException e) {
                future.completeExceptionally(new PersistenceException(e));
            } catch (PersistenceException e) {
                future.completeExceptionally(e);
            }
        }, executor);
        return future;
    }

    @Override
    public <T> CompletableFuture<Void> remove(RemovableStoredData<T, Connection> dataType, T toRemove) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.remove(toRemove, connection);
            } catch (SQLException | PersistenceException e) {
                Logger.logErr(e);
            }
        }, executor);
    }

    @Override
    public <T> CompletableFuture<Void> updateValue(UpdateableStoredData<T, Connection> dataType, T newValue) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.update(newValue, connection);
            } catch (SQLException | PersistenceException e) {
                Logger.logErr(e);
            }
        }, executor);
    }

    @Override
    public <T> CompletableFuture<Void> insertValue(InsertableStoredData<T, Connection> dataType, T value) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.insert(value, connection);
            } catch (SQLException | PersistenceException e) {
                Logger.logErr(e);
            }
        }, executor);
    }

    @Override
    public <T, U> CompletableFuture<List<T>> find(FindableStoredData<T, U, Connection> dataType, U searchObject) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                future.complete(dataType.find(searchObject, connection));
            } catch (SQLException e) {
                future.completeExceptionally(new PersistenceException(e));
            } catch (PersistenceException e) {
                future.completeExceptionally(e);
            }
        }, executor);
        return future;
    }

    @Override
    public <T> CompletableFuture<T> getSingleton(SingletonStoredData<T, Connection> dataType) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        final CompletableFuture<T> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                future.complete(dataType.value(connection));
            } catch (SQLException e) {
                future.completeExceptionally(new PersistenceException(e));
            } catch (PersistenceException e) {
                future.completeExceptionally(e);
            }
        }, executor);
        return future;
    }

    @Override
    public <T> CompletableFuture<Void> setSingleton(SingletonStoredData<T, Connection> dataType, T t) throws PersistenceException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Not initialized");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                dataType.set(t, connection);
            } catch (SQLException | PersistenceException e) {
                Logger.logErr(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.runAsync(() -> {
        }, executor);
    }
}
