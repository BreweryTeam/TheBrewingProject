package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.effect.DrunkStateDataType;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlDrunkStateDataType implements DrunkStateDataType<Connection> {

    public static final SqlDrunkStateDataType INSTANCE = new SqlDrunkStateDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/drunk_states");

    @Override
    public void update(Pair<DrunkStateImpl, UUID> newValue, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            DrunkStateImpl drunkState = newValue.first();
            preparedStatement.setInt(1, drunkState.alcohol());
            preparedStatement.setInt(2, drunkState.toxins());
            preparedStatement.setLong(3, drunkState.kickedTimestamp());
            preparedStatement.setLong(4, drunkState.timestamp());
            preparedStatement.setBytes(5, DecoderEncoder.asBytes(newValue.second()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void insert(Pair<DrunkStateImpl, UUID> value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            DrunkStateImpl drunkState = value.first();
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(value.second()));
            preparedStatement.setInt(2, drunkState.alcohol());
            preparedStatement.setInt(3, drunkState.toxins());
            preparedStatement.setLong(4, drunkState.kickedTimestamp());
            preparedStatement.setLong(5, drunkState.timestamp());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(UUID toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(toRemove));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Pair<DrunkStateImpl, UUID>> retrieveAll(Connection connection) throws PersistenceException {
        List<Pair<DrunkStateImpl, UUID>> drunks = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.SELECT_ALL))) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                drunks.add(new Pair<>(
                        new DrunkStateImpl(resultSet.getInt("alcohol_level"),
                                resultSet.getInt("toxin_level"),
                                resultSet.getLong("time_stamp"),
                                resultSet.getLong("kicked_timestamp")),
                        DecoderEncoder.asUuid(resultSet.getBytes("player_uuid"))
                ));
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        return drunks;
    }
}
