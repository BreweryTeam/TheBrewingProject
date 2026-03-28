package dev.jsinco.brewery.configuration.structure;

import dev.jsinco.brewery.api.breweries.BarrelType;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarrelTypeDefinitions extends OkaeriConfig {

    @CustomKey("barrel-types")
    public final List<BarrelTypeDefinition> barrelTypes = List.of();

    @Exclude
    private static BarrelTypeDefinitions instance;
    @Exclude
    private static BarrelTypeDefinitions defaultsInstance;

    public static List<BarrelType> allBarrelTypes() {
        boolean newlySaved = false;
        File barrelTypesFile = new File("plugins/TheBrewingProject", "barrel_types.yml");
        try {
            try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/barrel_types.yml")) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Internal file '/barrel_types.yml' not found");
                }
                if (!barrelTypesFile.exists()) {
                    if (!barrelTypesFile.createNewFile()) {
                        throw new IOException("Could not create file, even though did not exist: " + barrelTypesFile);
                    }
                }
            }
            if (!barrelTypesFile.exists()) {
                try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/barrel_types.yml")) {
                    if (inputStream == null) {
                        throw new FileNotFoundException("Internal file '/barrel_types.yml' not found");
                    }
                    try (OutputStream outputStream = new FileOutputStream(barrelTypesFile)) {
                        inputStream.transferTo(outputStream);
                    }
                }
            }
            if (!newlySaved) {
                instance = ConfigManager.create(BarrelTypeDefinitions.class, it -> {
                    it.configure(opts -> {
                        opts.bindFile(barrelTypesFile);
                        opts.configurer(new YamlSnakeYamlConfigurer());
                    });
                    it.load(false);
                });
            }
            try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/barrel_types.yml")) {
                defaultsInstance = ConfigManager.create(BarrelTypeDefinitions.class, it -> {
                    it.configure(opts -> {
                        opts.configurer(new YamlSnakeYamlConfigurer());
                    });
                    it.load(inputStream);
                });
            }
            List<BarrelType> barrelTypes = new ArrayList<>();
            instance.barrelTypes.stream()
                    .map(BarrelTypeDefinition::toBarrelType)
                    .forEach(barrelTypes::add);
            defaultsInstance.barrelTypes.stream()
                    .map(BarrelTypeDefinition::toBarrelType)
                    .filter(barrelType -> barrelTypes.stream().noneMatch(barrelType1 -> barrelType1.key().equals(barrelType.key())))
                    .forEach(barrelTypes::add);
            return Collections.unmodifiableList(barrelTypes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
