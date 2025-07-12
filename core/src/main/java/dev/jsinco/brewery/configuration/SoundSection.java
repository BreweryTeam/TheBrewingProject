package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.sound.SoundDefinition;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import static dev.jsinco.brewery.sound.SoundDefinitionSerializer.parseSoundDefinition;

@ConfigSerializable
public class SoundSection {

    public SoundDefinition barrelClose = parseSoundDefinition("minecraft:block.barrel.close/0.8;0.9");
    public SoundDefinition barrelOpen = parseSoundDefinition("minecraft:block.barrel.open/0.8;0.9");
    public SoundDefinition cauldronIngredientAddBrew = parseSoundDefinition("minecraft:block.pointed_dripstone.drip_water_into_cauldron/0.55;0.65");
    public SoundDefinition cauldronIngredientAdd = parseSoundDefinition("minecraft:entity.generic.splash/1.4;1.6");
    public SoundDefinition cauldronBrewExtract = parseSoundDefinition("minecraft:item.bottle.fill/0.9;1.0");
    public SoundDefinition distilleryAccess = parseSoundDefinition("minecraft:block.vault.fall");
    public SoundDefinition distilleryProcess = parseSoundDefinition("minecraft:block.brewing_stand.brew");

}
