package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.sound.SoundDefinition;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static dev.jsinco.brewery.sound.SoundDefinitionSerializer.parseSoundDefinition;

@ConfigSerializable
public record SoundSection(
        SoundDefinition barrelClose,
        SoundDefinition barrelOpen,
        SoundDefinition cauldronIngredientAddBrew,
        SoundDefinition cauldronIngredientAdd,
        SoundDefinition cauldronBrewExtract,
        SoundDefinition distilleryAccess,
        SoundDefinition distilleryProcess
) {

    public static final SoundSection DEFAULT = new SoundSection(
            parseSoundDefinition("minecraft:block.barrel.close/0.8;0.9"),
            parseSoundDefinition("minecraft:block.barrel.open/0.8;0.9"),
            parseSoundDefinition("minecraft:block.pointed_dripstone.drip_water_into_cauldron/0.55;0.65"),
            parseSoundDefinition("minecraft:entity.generic.splash/1.4;1.6"),
            parseSoundDefinition("minecraft:item.bottle.fill/0.9;1.0"),
            parseSoundDefinition("minecraft:block.vault.fall"),
            parseSoundDefinition("minecraft:block.brewing_stand.brew")
    );
}
