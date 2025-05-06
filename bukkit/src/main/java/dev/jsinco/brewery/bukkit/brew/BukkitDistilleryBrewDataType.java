package dev.jsinco.brewery.bukkit.brew;

import com.google.gson.JsonParser;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitDistilleryBrewDataType implements
        SqlStoredData.Insertable<Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext>>, SqlStoredData.Removable<Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext>>,
        SqlStoredData.Findable<Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext>, BreweryLocation>, SqlStoredData.Updateable<Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext>> {

    public static final BukkitDistilleryBrewDataType INSTANCE = new BukkitDistilleryBrewDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/distillery_brews");

    @Override
    public List<Pair<Brew, DistilleryContext>> find(BreweryLocation searchObject, Connection connection) throws PersistenceException {
        List<Pair<Brew, DistilleryContext>> output = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setInt(1, searchObject.x());
            preparedStatement.setInt(2, searchObject.y());
            preparedStatement.setInt(3, searchObject.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(searchObject.worldUuid()));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int pos = resultSet.getInt("pos");
                boolean isDistillate = resultSet.getBoolean("is_distillate");
                Brew brew = BrewImpl.SERIALIZER.deserialize(JsonParser.parseString(resultSet.getString("brew")).getAsJsonArray(), BukkitIngredientManager.INSTANCE);
                output.add(new Pair<>(brew, new DistilleryContext(searchObject.x(), searchObject.y(), searchObject.z(), searchObject.worldUuid(), pos, isDistillate)));
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        return output;
    }

    @Override
    public void insert(Pair<Brew, DistilleryContext> value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            DistilleryContext distilleryContext = value.second();
            Brew brew = value.first();
            preparedStatement.setInt(1, distilleryContext.uniqueX());
            preparedStatement.setInt(2, distilleryContext.uniqueY());
            preparedStatement.setInt(3, distilleryContext.uniqueZ());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(distilleryContext.worldUuid()));
            preparedStatement.setInt(5, distilleryContext.inventoryPos());
            preparedStatement.setBoolean(6, distilleryContext.distillate());
            preparedStatement.setString(7, BrewImpl.SERIALIZER.serialize(brew).toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Pair<Brew, DistilleryContext> toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            DistilleryContext distilleryContext = toRemove.second();
            preparedStatement.setInt(1, distilleryContext.uniqueX());
            preparedStatement.setInt(2, distilleryContext.uniqueY());
            preparedStatement.setInt(3, distilleryContext.uniqueZ());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(distilleryContext.worldUuid()));
            preparedStatement.setInt(5, distilleryContext.inventoryPos());
            preparedStatement.setBoolean(6, distilleryContext.distillate());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void update(Pair<Brew, DistilleryContext> newValue, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            Brew brew = newValue.first();
            DistilleryContext distilleryContext = newValue.second();
            preparedStatement.setString(1, BrewImpl.SERIALIZER.serialize(brew).toString());
            preparedStatement.setInt(2, distilleryContext.uniqueX());
            preparedStatement.setInt(3, distilleryContext.uniqueY());
            preparedStatement.setInt(4, distilleryContext.uniqueZ());
            preparedStatement.setBytes(5, DecoderEncoder.asBytes(distilleryContext.worldUuid()));
            preparedStatement.setInt(6, distilleryContext.inventoryPos());
            preparedStatement.setBoolean(7, distilleryContext.distillate());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public record DistilleryContext(int uniqueX, int uniqueY, int uniqueZ, UUID worldUuid, int inventoryPos,
                                    boolean distillate) {

    }
}
