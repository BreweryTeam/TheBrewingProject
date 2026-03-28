package dev.jsinco.brewery.configuration.structure;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class StructureMatchers extends OkaeriConfig {

    @CustomKey("structure-matchers")
    List<StructureMatcherDefinition> structureMatcherDefinitions = List.of();

    public static List<StructureMatcherDefinition> matchers(File dataFolder) {
        File barrelTypesFile = new File(dataFolder, "structures.yml");
        try {
            try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/structures.yml")) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Internal file '/structures.yml' not found");
                }

            }
            if (!barrelTypesFile.exists()) {
                try (InputStream inputStream = BarrelTypeDefinition.class.getResourceAsStream("/structures.yml")) {
                    if (inputStream == null) {
                        throw new FileNotFoundException("Internal file '/structures.yml' not found");
                    }
                    if (!barrelTypesFile.createNewFile()) {
                        throw new IOException("Could not create file, even though it existed: " + barrelTypesFile);
                    }
                    try (OutputStream outputStream = new FileOutputStream(barrelTypesFile)) {
                        inputStream.transferTo(outputStream);
                    }
                }

            }
            return ConfigManager.create(StructureMatchers.class, it -> {
                it.configure(opts -> {
                    opts.bindFile(barrelTypesFile);
                    opts.configurer(new YamlSnakeYamlConfigurer());
                });
                it.load(false);
            }).structureMatcherDefinitions;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
