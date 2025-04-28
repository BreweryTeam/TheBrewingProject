package dev.jsinco.brewery.brew;

import com.google.gson.JsonParser;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.FileUtil;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BarrelBrewDataType<I> implements
        SqlStoredData.Insertable<Pair<Brew, BarrelBrewDataType.BarrelContext>>, SqlStoredData.Removable<Pair<Brew, BarrelBrewDataType.BarrelContext>>,
        SqlStoredData.Findable<Pair<Brew, Integer>, BreweryLocation>, SqlStoredData.Updateable<Pair<Brew, BarrelBrewDataType.BarrelContext>> {
    protected BarrelBrewDataType() {
    }

    @Override
    public void insert(Pair<Brew, BarrelContext> value, Connection connection) throws PersistenceException {
        String statementString = FileUtil.readInternalResource("/database/generic/barrel_brews_insert.sql");
        try (PreparedStatement preparedStatement = connection.prepareStatement(statementString)) {
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/database/generic/barrel_brews_remove.sql"))) {
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

    private BrewImpl brewFromResultSet(ResultSet resultSet) throws SQLException {
        return BrewImpl.SERIALIZER.deserialize(JsonParser.parseString(resultSet.getString("brew")).getAsJsonArray(), getIngredientManager());
    }

    protected abstract IngredientManager<I> getIngredientManager();

    private BarrelContext contextFromResultSet(ResultSet resultSet) throws SQLException {
        return new BarrelContext(resultSet.getInt("sign_x"), resultSet.getInt("sign_y"), resultSet.getInt("sign_z"), resultSet.getInt("pos"), DecoderEncoder.asUuid(resultSet.getBytes("world_uuid")));
    }

    @Override
    public List<Pair<Brew, Integer>> find(BreweryLocation signLocation, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/database/generic/barrel_brews_find.sql"))) {
            preparedStatement.setInt(1, signLocation.x());
            preparedStatement.setInt(2, signLocation.y());
            preparedStatement.setInt(3, signLocation.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(signLocation.worldUuid()));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Pair<Brew, Integer>> output = new ArrayList<>();
            while (resultSet.next()) {
                output.add(new Pair<>(brewFromResultSet(resultSet), resultSet.getInt("pos")));
            }
            return output;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void update(Pair<Brew, BarrelContext> newValue, Connection connection) throws PersistenceException {
        String statementString = FileUtil.readInternalResource("/database/generic/barrel_brews_update.sql");
        try (PreparedStatement preparedStatement = connection.prepareStatement(statementString)) {
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
