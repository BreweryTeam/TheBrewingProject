package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.bukkit.util.LocationUtil;
import dev.jsinco.brewery.vector.BreweryLocation;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Arrays;
import java.util.UUID;

public class BreweryLocationSerializer implements ObjectSerializer<BreweryLocation.Uncompiled> {

    @Override
    public boolean supports(@NonNull Class<? super BreweryLocation.Uncompiled> type) {
        return BreweryLocation.Uncompiled.class == type;
    }

    @Override
    public void serialize(@NonNull BreweryLocation.Uncompiled object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        BreweryLocation location = object.get(Bukkit.getWorlds().stream().map(World::getUID).toList());
        if (location == null) {
            location = new BreweryLocation(0, 0, 0, UUID.randomUUID());
        }
        World world = Bukkit.getWorld(LocationUtil.resolveWorld(location.worldUuid().toString()));
        data.setValue(
                String.format("%s, %d, %d, %d", world == null ? LocationUtil.resolveWorld(location.worldUuid().toString()) : world.getName(), location.x(), location.y(), location.z())
        );
    }

    @Override
    public BreweryLocation.Uncompiled deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String string = data.getValue(String.class);
        if (string == null) {
            throw new IllegalArgumentException("Can not deserialize empty node");
        }
        String[] split = Arrays.stream(string.split(",")).map(String::trim).toArray(String[]::new);
        if (split.length != 4) {
            throw new IllegalArgumentException("Expected location of format world, x, y, z");
        }
        try {
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return ignored -> new BreweryLocation(x, y, z, UUID.fromString(split[0]));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }
}
