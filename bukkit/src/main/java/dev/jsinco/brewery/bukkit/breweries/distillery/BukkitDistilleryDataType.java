package dev.jsinco.brewery.bukkit.breweries.distillery;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BukkitDistilleryBrewDataType;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.structure.BreweryStructure;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.Logging;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Matrix3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BukkitDistilleryDataType implements SqlStoredData.Findable<BukkitDistillery, UUID>, SqlStoredData.Insertable<BukkitDistillery>, SqlStoredData.Removable<BukkitDistillery>, SqlStoredData.Updateable<BukkitDistillery> {

    public static final BukkitDistilleryDataType INSTANCE = new BukkitDistilleryDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/distilleries");

    @Override
    public void insert(BukkitDistillery value, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            PlacedBreweryStructure<BukkitDistillery> structure = value.getStructure();
            BreweryLocation origin = BukkitAdapter.toBreweryLocation(structure.getWorldOrigin());
            BreweryLocation unique = structure.getUnique();
            preparedStatement.setInt(1, origin.x());
            preparedStatement.setInt(2, origin.y());
            preparedStatement.setInt(3, origin.z());
            preparedStatement.setInt(4, unique.x());
            preparedStatement.setInt(5, unique.y());
            preparedStatement.setInt(6, unique.z());
            preparedStatement.setBytes(7, DecoderEncoder.asBytes(origin.worldUuid()));
            preparedStatement.setString(8, DecoderEncoder.serializeTransformation(structure.getTransformation()));
            preparedStatement.setString(9, structure.getStructure().getName());
            preparedStatement.setLong(10, value.getStartTime());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(BukkitDistillery toRemove, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            BreweryLocation unique = toRemove.getStructure().getUnique();
            preparedStatement.setInt(1, unique.x());
            preparedStatement.setInt(2, unique.y());
            preparedStatement.setInt(3, unique.z());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(unique.worldUuid()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<BukkitDistillery> find(UUID worldUuid, Connection connection) throws PersistenceException {
        List<BukkitDistillery> output = new ArrayList<>();
        World world = Bukkit.getWorld(worldUuid);
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(worldUuid));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int originX = resultSet.getInt("origin_x");
                int originY = resultSet.getInt("origin_y");
                int originZ = resultSet.getInt("origin_z");
                Location structureOrigin = new Location(world, originX, originY, originZ);
                String structureName = resultSet.getString("format");
                Optional<BreweryStructure> breweryStructure = TheBrewingProject.getInstance().getStructureRegistry().getStructure(structureName);
                if (breweryStructure.isEmpty()) {
                    Logging.warning("Could not find format '" + structureName + "' skipping distillery at: " + structureOrigin);
                    continue;
                }
                Matrix3d transformation = DecoderEncoder.deserializeTransformation(resultSet.getString("transformation"));
                PlacedBreweryStructure<BukkitDistillery> placedBreweryStructure = new PlacedBreweryStructure<>(breweryStructure.get(), transformation, structureOrigin);
                int startTime = resultSet.getInt("start_time");
                BukkitDistillery bukkitDistillery = new BukkitDistillery(placedBreweryStructure, startTime);
                placedBreweryStructure.setHolder(bukkitDistillery);
                output.add(bukkitDistillery);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        for (BukkitDistillery distillery : output) {
            List<Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext>> contents = BukkitDistilleryBrewDataType.INSTANCE.find(distillery.getStructure().getUnique(), connection);
            for (Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext> content : contents) {
                BukkitDistilleryBrewDataType.DistilleryContext context = content.second();
                BrewInventory inventory = context.distillate() ? distillery.getDistillate() : distillery.getMixture();
                inventory.set(content.first(), context.inventoryPos());
            }
        }
        return output;
    }

    @Override
    public void update(BukkitDistillery newValue, Connection connection) throws PersistenceException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.UPDATE))) {
            long startTime = newValue.getStartTime();
            BreweryLocation unique = newValue.getStructure().getUnique();
            preparedStatement.setLong(1, startTime);
            preparedStatement.setInt(2, unique.x());
            preparedStatement.setInt(3, unique.y());
            preparedStatement.setInt(4, unique.z());
            preparedStatement.setBytes(5, DecoderEncoder.asBytes(unique.worldUuid()));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
