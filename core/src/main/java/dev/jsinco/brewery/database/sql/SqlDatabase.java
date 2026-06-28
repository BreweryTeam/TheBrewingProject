package dev.jsinco.brewery.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.database.Session;
import dev.jsinco.brewery.database.SessionType;
import dev.jsinco.brewery.util.FileUtil;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SqlDatabase implements PersistenceHandler {

    private static final int BREWERY_DATABASE_VERSION = 3;
    private final DatabaseDriver driver;
    private HikariDataSource hikariDataSource;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public SqlDatabase(DatabaseDriver databaseDriver) {
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

    public Connection getConnection() throws PersistenceException {
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private static @NonNull HikariConfig getHikariConfigForSqlite(File dataFolder) throws IOException {
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
            case 2 -> {
                for (String statement : FileUtil.readInternalResource("/database/migration/add_cauldron_type_migration.sql").split(";")) {
                    connection.prepareStatement(statement + ";").execute();
                }
            }
            default -> throw new IllegalStateException("Unimplemented migration from version: " + version);
        }
    }


    @Override
    public CompletableFuture<Void> flush() {
        return CompletableFuture.runAsync(() -> {
        }, executor);
    }

    @Override
    public <T extends Session<T>> T startSession(SessionType<T> sessionType) throws PersistenceException {
        return sessionType.retrieve(executor, this);
    }

    @Override
    public DatabaseDriver driver() {
        return driver;
    }
}
