package dev.jsinco.brewery.bukkit.breweries;

import com.google.gson.JsonParser;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitCauldronDataType implements SqlStoredData.Findable<BukkitCauldron, UUID>, SqlStoredData.Insertable<BukkitCauldron>, SqlStoredData.Updateable<BukkitCauldron>, SqlStoredData.Removable<BukkitCauldron> {

    public static final BukkitCauldronDataType INSTANCE = new BukkitCauldronDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/cauldrons");

    @Override
    public void insert(BukkitCauldron value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            BreweryLocation location = value.position();
            preparedStatement.setInt(1, location.x());
            preparedStatement.setInt(2, location.y());
            preparedStatement.setInt(3, location.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(location.worldUuid()));
            preparedStatement.setString(5, BrewImpl.SERIALIZER.serialize(value.getBrew()).toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void update(BukkitCauldron newValue, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            BreweryLocation location = newValue.position();
            preparedStatement.setString(1, BrewImpl.SERIALIZER.serialize(newValue.getBrew()).toString());
            preparedStatement.setInt(2, location.x());
            preparedStatement.setInt(3, location.y());
            preparedStatement.setInt(4, location.z());
            preparedStatement.setBytes(5, DecoderEncoder.asBytes(location.worldUuid()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(BukkitCauldron toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            BreweryLocation location = toRemove.position();
            preparedStatement.setInt(1, location.x());
            preparedStatement.setInt(2, location.y());
            preparedStatement.setInt(3, location.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(location.worldUuid()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<BukkitCauldron> find(UUID worldUuid, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(worldUuid));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<BukkitCauldron> cauldrons = new ArrayList<>();
            while (resultSet.next()) {
                int x = resultSet.getInt("cauldron_x");
                int y = resultSet.getInt("cauldron_y");
                int z = resultSet.getInt("cauldron_z");
                Brew brew = BrewImpl.SERIALIZER.deserialize(JsonParser.parseString(resultSet.getString("brew")).getAsJsonArray(), BukkitIngredientManager.INSTANCE);
                cauldrons.add(new BukkitCauldron(brew, new BreweryLocation(x, y, z, worldUuid)));
            }
            return cauldrons;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
