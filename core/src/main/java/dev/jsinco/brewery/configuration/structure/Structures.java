package dev.jsinco.brewery.configuration.structure;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;

import java.util.List;

public class Structures extends OkaeriConfig {

    @CustomKey("structure-matchers")
    public final List<StructureMatcherDefinition> structureMatcherDefinitions = List.of();
}
