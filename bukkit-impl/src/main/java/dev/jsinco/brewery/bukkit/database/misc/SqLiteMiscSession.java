package dev.jsinco.brewery.bukkit.database.misc;

import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceSupplier;
import dev.jsinco.brewery.database.sql.SqlStatements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record SqLiteMiscSession(Executor executor,
                                PersistenceSupplier<Connection> connectionSupplier) implements MiscSession {

    private static final SqlStatements TIME_STATEMENTS = new SqlStatements("/database/generic/time");

    @Override
    public CompletableFuture<Void> setTime(long time) {
        return execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(TIME_STATEMENTS.get(SqlStatements.Type.SET_SINGLETON))) {
                preparedStatement.setLong(1, time);
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Long> getTime() {
        return fetch(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(TIME_STATEMENTS.get(SqlStatements.Type.GET_SINGLETON))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getLong("time");
                } else {
                    return 0L;
                }
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }
}
