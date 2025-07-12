package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.sound.SoundDefinition;
import dev.jsinco.brewery.sound.SoundDefinitionSerializer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class SoundSection {

    public SoundDefinition barrelClose = parseSoundSetting("minecraft:block.barrel.close/0.8;0.9");
    public SoundDefinition barrelOpen = parseSoundSetting("minecraft:block.barrel.open/0.8;0.9");
    public SoundDefinition cauldronIngredientAddBrew = parseSoundSetting("minecraft:block.pointed_dripstone.drip_water_into_cauldron/0.55;0.65");
    public SoundDefinition cauldronIngredientAdd = parseSoundSetting("minecraft:entity.generic.splash/1.4;1.6");
    public SoundDefinition cauldronBrewExtract = parseSoundSetting("minecraft:item.bottle.fill/0.9;1.0");
    public SoundDefinition distilleryAccess = parseSoundSetting("minecraft:block.vault.fall");
    public SoundDefinition distilleryProcess = parseSoundSetting("minecraft:block.brewing_stand.brew");

    private static SoundDefinition parseSoundSetting(String string) {
        return new SoundDefinition(List.of(SoundDefinitionSerializer.parseSoundSetting(string)));
    }
}
