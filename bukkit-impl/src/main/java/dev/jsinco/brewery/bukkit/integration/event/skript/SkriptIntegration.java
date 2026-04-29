package dev.jsinco.brewery.bukkit.integration.event.skript;

import ch.njol.skript.Skript;
import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.properties.PropertyRegistry;

import java.util.List;
import java.util.Optional;

public class SkriptIntegration implements EventIntegration<SkriptIntegration.SkriptIntegrationEvent> {


    @Override
    public Class<SkriptIntegrationEvent> eClass() {
        return SkriptIntegrationEvent.class;
    }

    @Override
    public List<BreweryKey> listEventKeys() {
        return List.of();
    }

    @Override
    public Optional<SkriptIntegrationEvent> convertToEvent(EventData eventData) {
        return Optional.empty();
    }

    @Override
    public EventData convertToData(SkriptIntegrationEvent event) {
        return null;
    }

    @Override
    public String getId() {
        return "skript";
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("org.skriptlang.skript.addon.SkriptAddon");
    }

    @Override
    public void onLoad() {
        SkriptAddon skriptAddon = Skript.instance().registerAddon(TheBrewingProject.class, "TheBrewingProject-Skript");
        PropertyRegistry propertyRegistry = skriptAddon.registry(PropertyRegistry.class);
        propertyRegistry.register()
        TbpSyntaxRegistry tbpRegistry = skriptAddon.registry(TbpSyntaxRegistry.class, TbpSyntaxRegistry::new);
        EventValueRegistry eventRegistry = skriptAddon.registry(EventValueRegistry.class);
    }

    public record SkriptIntegrationEvent() implements IntegrationEvent {

        @Override
        public void run(Holder.Player player) {

        }

        @Override
        public BreweryKey key() {
            return null;
        }

        @Override
        public Component displayName() {
            return null;
        }

        @Override
        public EventProbability probability() {
            return null;
        }
    }
}
