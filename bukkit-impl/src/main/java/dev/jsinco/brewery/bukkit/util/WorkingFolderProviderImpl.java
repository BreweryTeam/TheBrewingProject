package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.util.WorkingFolderProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorkingFolderProviderImpl implements WorkingFolderProvider {


    @Override
    public File getWorkingFolder() {
        TheBrewingProject instance = TheBrewingProject.getInstance();
        if (instance == null) {
            try {
                return Files.createTempDirectory("tbp").toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance.getDataFolder();
    }
}
