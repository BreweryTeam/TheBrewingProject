package dev.jsinco.brewery.bukkit.breweries.barrel;

import dev.jsinco.brewery.brew.BarrelBrewDataType;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BukkitBarrelBrewDataType;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.structure.BreweryStructure;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.database.sql.SqlStoredData;
import dev.jsinco.brewery.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.joml.Matrix3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BukkitBarrelDataType implements SqlStoredData.Findable<BukkitBarrel, UUID>, SqlStoredData.Removable<BukkitBarrel>, SqlStoredData.Insertable<BukkitBarrel> {
    public static final BukkitBarrelDataType INSTANCE = new BukkitBarrelDataType();
    private final SqlStatements statements = new SqlStatements("/database/generic/barrels");

    @Override
    public void insert(BukkitBarrel value, Connection connection) throws PersistenceException {

        PlacedBreweryStructure<BukkitBarrel> placedStructure = value.getStructure();
        BreweryStructure structure = placedStructure.getStructure();
        Location origin = placedStructure.getWorldOrigin();
        UUID worldUuid = value.getWorld().getUID();
        Location signLocation = value.getUniqueLocation();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.INSERT))) {
            preparedStatement.setInt(1, origin.getBlockX());
            preparedStatement.setInt(2, origin.getBlockY());
            preparedStatement.setInt(3, origin.getBlockZ());
            preparedStatement.setInt(4, signLocation.getBlockX());
            preparedStatement.setInt(5, signLocation.getBlockY());
            preparedStatement.setInt(6, signLocation.getBlockZ());
            preparedStatement.setBytes(7, DecoderEncoder.asBytes(worldUuid));
            preparedStatement.setString(8, DecoderEncoder.serializeTransformation(placedStructure.getTransformation()));
            preparedStatement.setString(9, structure.getName());
            preparedStatement.setString(10, value.getType().key().toString());
            preparedStatement.setInt(11, value.getSize());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        for (Pair<Brew, Integer> brew : value.getBrews()) {
            BarrelBrewDataType.BarrelContext context = new BarrelBrewDataType.BarrelContext(signLocation.getBlockX(),
                    signLocation.getBlockY(), signLocation.getBlockZ(), brew.second(), signLocation.getWorld().getUID());
            TheBrewingProject.getInstance().getDatabase().insertValue(BukkitBarrelBrewDataType.INSTANCE, new Pair<>(brew.first(), context));
        }
    }

    @Override
    public void remove(BukkitBarrel toRemove, Connection connection) throws PersistenceException {
        UUID worldUuid = toRemove.getWorld().getUID();
        Location signLocation = toRemove.getUniqueLocation();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.DELETE))) {
            preparedStatement.setInt(1, signLocation.getBlockX());
            preparedStatement.setInt(2, signLocation.getBlockY());
            preparedStatement.setInt(3, signLocation.getBlockZ());
            preparedStatement.setBytes(4, DecoderEncoder.asBytes(worldUuid));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<BukkitBarrel> find(UUID world, Connection connection) throws PersistenceException {
        List<BukkitBarrel> output = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(statements.get(SqlStatements.Type.FIND))) {
            preparedStatement.setBytes(1, DecoderEncoder.asBytes(world));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Location worldOrigin = new Location(Bukkit.getWorld(world), resultSet.getInt("origin_x"), resultSet.getInt("origin_y"), resultSet.getInt("origin_z"));
                Location uniqueLocation = new Location(Bukkit.getWorld(world), resultSet.getInt("unique_x"), resultSet.getInt("unique_y"), resultSet.getInt("unique_z"));
                Matrix3d transform = DecoderEncoder.deserializeTransformation(resultSet.getString("transformation"));
                String format = resultSet.getString("format");
                BarrelType type = Registry.BARREL_TYPE.get(BreweryKey.parse(resultSet.getString("barrel_type")));
                if (type == null) {
                    Logging.warning("Unknown barrel type '" + resultSet.getString("barrel_type") + "' for structure at: " + uniqueLocation);
                    continue;
                }
                int size = resultSet.getInt("size");

                Optional<BreweryStructure> breweryStructureOptional = TheBrewingProject.getInstance().getStructureRegistry().getStructure(format);
                if (breweryStructureOptional.isEmpty()) {
                    Logging.warning("Could not find format '" + format + "' skipping barrel at: " + uniqueLocation);
                    continue;
                }
                PlacedBreweryStructure<BukkitBarrel> structure = new PlacedBreweryStructure<>(breweryStructureOptional.get(), transform, worldOrigin);
                BukkitBarrel barrel = new BukkitBarrel(uniqueLocation, structure, size, type);
                structure.setHolder(barrel);
                output.add(barrel);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        for (BukkitBarrel barrel : output) {
            BrewInventory barrelInventory = barrel.getInventory();
            FutureUtil.mergeFutures(BukkitBarrelBrewDataType.INSTANCE.find(BukkitAdapter.toBreweryLocation(barrel.getUniqueLocation()), connection))
                    .thenAcceptAsync(brews -> brews.forEach(pair -> barrelInventory.set(pair.first(), pair.second()))).join();
        }
        return output;
    }
}
