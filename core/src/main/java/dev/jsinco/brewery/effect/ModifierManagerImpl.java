package dev.jsinco.brewery.effect;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.effect.modifier.ModifierManager;
import dev.jsinco.brewery.api.effect.modifier.ModifierTooltip;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;

import java.util.List;
import java.util.Optional;

public class ModifierManagerImpl implements ModifierManager {
    @Override
    public List<DrunkenModifier> allModifiers() {
        return DrunkenModifierSection.modifiers().drunkenModifiers();
    }

    @Override
    public Optional<DrunkenModifier> getModifier(String name) {
        return DrunkenModifierSection.modifiers().optionalModifier(name);
    }

    @Override
    public List<ModifierTooltip> allTooltips() {
        return DrunkenModifierSection.modifiers().drunkenTooltips();
    }

    @Override
    public List<ModifierDisplay> allDisplays() {
        return DrunkenModifierSection.modifiers().drunkenDisplays();
    }
}
