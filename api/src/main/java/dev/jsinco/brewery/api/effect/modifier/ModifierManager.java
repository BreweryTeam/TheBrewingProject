package dev.jsinco.brewery.api.effect.modifier;

import java.util.List;
import java.util.Optional;

public interface ModifierManager {


    List<DrunkenModifier> allModifiers();

    Optional<DrunkenModifier> getModifier(String name);

    List<ModifierTooltip> allTooltips();

    List<ModifierDisplay> allDisplays();
}
