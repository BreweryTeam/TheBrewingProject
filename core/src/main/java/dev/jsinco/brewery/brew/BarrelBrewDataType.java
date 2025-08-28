package dev.jsinco.brewery.brew;

import com.google.gson.JsonParser;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.vector.BreweryLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class BarrelBrewDataType<I> implements
        SqlStoredData.Insertable<Pair<Brew, BarrelBrewDataType.BarrelContext>>, SqlStoredData.Removable<Pair<Brew, BarrelBrewDataType.BarrelContext>>,
        SqlStoredData.Findable<CompletableFuture<Pair<Brew, Integer>>, BreweryLocation>, SqlStoredData.Updateable<Pair<Brew, BarrelBrewDataType.BarrelContext>> {
    private final SqlStatements statements = new SqlStatements("/database/generic/barrel_brews");

    protected BarrelBrewDataType() {
    }

    @Override
    public void insert(Pair<Brew, BarrelContext> value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            BarrelContext context = value.second();
            Brew brew = value.first();
            preparedStatement.setInt(1, context.uniqueX);
            preparedStatement.setInt(2, context.uniqueY);
            preparedStatement.setInt(3, context.uniqueZ);
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(context.worldUuid));
            preparedStatement.setInt(5, context.inventoryPos);
            preparedStatement.setString(6, BrewImpl.SERIALIZER.serialize(brew).toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(Pair<Brew, BarrelContext> toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            BarrelContext context = toRemove.second();
            preparedStatement.setInt(1, context.uniqueX);
            preparedStatement.setInt(2, context.uniqueY);
            preparedStatement.setInt(3, context.uniqueZ);
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(context.worldUuid));
            preparedStatement.setInt(5, context.inventoryPos);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    private CompletableFuture<Brew> brewFromResultSet(ResultSet resultSet) throws SQLException {
        return BrewImpl.SERIALIZER.deserialize(JsonParser.parseString(resultSet.getString("brew")).getAsJsonArray(), getIngredientManager());
    }

    protected abstract IngredientManager<I> getIngredientManager();

    private BarrelContext contextFromResultSet(ResultSet resultSet) throws SQLException {
        return new BarrelContext(resultSet.getInt("sign_x"), resultSet.getInt("sign_y"), resultSet.getInt("sign_z"), resultSet.getInt("pos"), DecoderEncoder.asUuid(resultSet.getBytes("world_uuid")));
    }

    @Override
    public List<CompletableFuture<Pair<Brew, Integer>>> find(BreweryLocation signLocation, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setInt(1, signLocation.x());
            preparedStatement.setInt(2, signLocation.y());
            preparedStatement.setInt(3, signLocation.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(signLocation.worldUuid()));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<CompletableFuture<Pair<Brew, Integer>>>  output = new ArrayList<>();
            while (resultSet.next()) {
                final int pos = resultSet.getInt("pos");
                output.add(brewFromResultSet(resultSet).thenApplyAsync(brew -> new Pair<>(brew, pos)));
            }
            return output;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void update(Pair<Brew, BarrelContext> newValue, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            BarrelContext context = newValue.second();
            Brew brew = newValue.first();
            preparedStatement.setString(1, BrewImpl.SERIALIZER.serialize(brew).toString());
            preparedStatement.setInt(2, context.uniqueX);
            preparedStatement.setInt(3, context.uniqueY);
            preparedStatement.setInt(4, context.uniqueZ);
            preparedStatement.setBytes(5, DecoderEncoder.asBytes(context.worldUuid));
            preparedStatement.setInt(6, context.inventoryPos);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public record BarrelContext(int uniqueX, int uniqueY, int uniqueZ, int inventoryPos, UUID worldUuid) {
    }
}
