package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BreweryTimeDataType implements SqlStoredData.Singleton<Long> {

    public static final BreweryTimeDataType INSTANCE = new BreweryTimeDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/time");

    @Override
    public Long value(Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.GET_SINGLETON))) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("time");
            } else {
                return 0L;
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void set(Long time, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.SET_SINGLETON))) {
            preparedStatement.setLong(1, time);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
