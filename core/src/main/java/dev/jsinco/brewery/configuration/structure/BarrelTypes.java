package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.util.Logger;
import me.sparky983.warp.Configuration;
import me.sparky983.warp.ConfigurationException;
import me.sparky983.warp.Property;
import me.sparky983.warp.Warp;
import me.sparky983.warp.yaml.YamlConfigurationSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public interface BarrelTypes {

    @Property("barrel-types")
    List<BarrelTypeDefinition> barrelTypes();

    static List<BarrelType> allBarrelTypes() {
        List<BarrelType> barrelTypes = new ArrayList<>();
        boolean newlySaved = false;
        File barrelTypesFile = new File("./plugins/TheBrewingProject", "barrel_types.yml");
        try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/barrel_types.yml")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Internal file '/barrel_types.yml' not found");
            }
            if (!barrelTypesFile.exists()) {
                if (barrelTypesFile.createNewFile()) {
                    throw new IOException("Could not create file, even though it existed: " + barrelTypesFile);
                }
                inputStream.mark(Short.MAX_VALUE);
                try (OutputStream outputStream = new FileOutputStream(barrelTypesFile)) {
                    inputStream.transferTo(outputStream);
                }
                inputStream.reset();
                newlySaved = true;
            }
            if (!newlySaved) {
                Warp.builder(BarrelTypes.class)
                        .source(YamlConfigurationSource.read(barrelTypesFile))
                        .build()
                        .barrelTypes()
                        .stream()
                        .map(BarrelTypeDefinition::toBarrelType)
                        .forEach(barrelTypes::add);
            }
            Warp.builder(BarrelTypes.class)
                    .source(YamlConfigurationSource.read(inputStream))
                    .build()
                    .barrelTypes()
                    .stream()
                    .map(BarrelTypeDefinition::toBarrelType)
                    .filter(barrelType -> barrelTypes.stream().noneMatch(type -> type.key().equals(barrelType.key())))
                    .forEach(barrelTypes::add);
            return barrelTypes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            Logger.logErr(e.getMessage());
            throw new RuntimeException("Unable to read barrel types, read above exception");
        }
    }
}
