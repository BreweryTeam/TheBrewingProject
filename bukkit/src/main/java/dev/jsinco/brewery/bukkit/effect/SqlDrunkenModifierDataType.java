package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.util.DecoderEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlDrunkenModifierDataType implements SqlStoredData.Insertable<Pair<SqlDrunkenModifierDataType.Data, Double>>, SqlStoredData.Removable<SqlDrunkenModifierDataType.Data>
        , SqlStoredData.Findable<Pair<DrunkenModifier, Double>, UUID> {

    public static final SqlDrunkenModifierDataType INSTANCE = new SqlDrunkenModifierDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/modifiers");

    @Override
    public void insert(Pair<Data, Double> value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(value.first().playerUuid()));
            preparedStatement.setString(2, value.first().modifier().name());
            preparedStatement.setDouble(3, value.second());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Data toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(toRemove.playerUuid()));
            preparedStatement.setString(2, toRemove.modifier().name());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Pair<DrunkenModifier, Double>> find(UUID searchObject, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(searchObject));
            ResultSet query = preparedStatement.executeQuery();
            List<Pair<DrunkenModifier, Double>> modifiers = new ArrayList<>();
            while (query.next()) {
                String modifierName = query.getString("modifier_name");
                double value = query.getDouble("value");
                DrunkenModifierSection.modifiers().drunkenModifiers().stream().filter(modifier -> modifier.name().equals(modifierName))
                        .findAny()
                        .map(modifier -> new Pair<>(modifier, value))
                        .ifPresent(modifiers::add);
            }
            return modifiers;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public record Data(DrunkenModifier modifier, UUID playerUuid) {
    }
}
