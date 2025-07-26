package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.configuration.serializers.SoundDefinitionSerializer;
import dev.jsinco.brewery.sound.SoundDefinition;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class SoundSection extends OkaeriConfig {

    @CustomKey("barrel-close")
    private SoundDefinition barrelClose = parseSoundSetting("minecraft:block.barrel.close/0.8;0.9");

    @CustomKey("barrel-open")
    private SoundDefinition barrelOpen = parseSoundSetting("minecraft:block.barrel.open/0.8;0.9");

    @CustomKey("cauldron-ingredient-add-brew")
    private SoundDefinition cauldronIngredientAddBrew = parseSoundSetting("minecraft:block.pointed_dripstone.drip_water_into_cauldron/0.55;0.65");

    @CustomKey("cauldron-ingredient-add")
    private SoundDefinition cauldronIngredientAdd = parseSoundSetting("minecraft:entity.generic.splash/1.4;1.6");

    @CustomKey("cauldron-brew-extract")
    private SoundDefinition cauldronBrewExtract = parseSoundSetting("minecraft:item.bottle.fill/0.9;1.0");

    @CustomKey("distillery-access")
    private SoundDefinition distilleryAccess = parseSoundSetting("minecraft:block.vault.fall");

    @CustomKey("distillery-process")
    private SoundDefinition distilleryProcess = parseSoundSetting("minecraft:block.brewing_stand.brew");

    private static SoundDefinition parseSoundSetting(String string) {
        return new SoundDefinition(List.of(SoundDefinitionSerializer.parseSoundSetting(string)));
    }
}
