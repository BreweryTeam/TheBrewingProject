package dev.jsinco.brewery.bukkit.integration.event;

import bodyhealth.api.BodyHealthAPI;
import bodyhealth.core.BodyPart;
import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BodyHealthIntegration implements EventIntegration<BodyHealthIntegration.BodyHealthEvent> {
    private static final Key VALUE = TheBrewingProject.key("value");
    private static final Key PERCENT = TheBrewingProject.key("percent");
    private static final Key FORCE = TheBrewingProject.key("force");

    @Override
    public Class<BodyHealthEvent> eClass() {
        return BodyHealthEvent.class;
    }

    @Override
    public List<BreweryKey> listEventKeys() {
        List<BreweryKey> output = new ArrayList<>();
        for (BodyPart bodyPart : BodyPart.values()) {
            for (BodyPartChangeType bodyPartChangeType : BodyPartChangeType.values()) {
                output.add(toKey(bodyPartChangeType, bodyPart));
            }
        }
        for (BodyPartChangeType changeType : BodyPartChangeType.values()) {
            output.add(toKey(changeType, null));
        }
        return output;
    }

    @Override
    public Optional<BodyHealthEvent> convertToEvent(EventData eventData) {
        Double value = eventData.data(VALUE, MetaDataType.STRING_TO_DOUBLE);
        double nonNullValue = value == null ? 10D : value;
        Boolean forced = eventData.data(FORCE, MetaDataType.STRING_TO_BOOLEAN);
        Boolean percent = eventData.data(PERCENT, MetaDataType.STRING_TO_BOOLEAN);
        BreweryKey namespacedKey = eventData.key();
        String key = namespacedKey.key();
        Optional<BodyPartChangeType> optionalBodyPartChangeType = Arrays.stream(BodyPartChangeType.values())
                .filter(bodyPartChangeType -> key.startsWith(bodyPartChangeType.name().toLowerCase(Locale.ROOT)))
                .findAny();
        Optional<BodyPart> optionalBodyPart = Arrays.stream(BodyPart.values())
                .filter(bodyPart -> key.endsWith(bodyPart.name().toLowerCase(Locale.ROOT)))
                .findAny();
        if (optionalBodyPartChangeType.isEmpty() || optionalBodyPart.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BodyHealthEvent(
                optionalBodyPartChangeType.get(),
                optionalBodyPart.get(),
                nonNullValue,
                percent == null || percent,
                forced != null && forced
        ));
    }

    @Override
    public EventData convertToData(BodyHealthEvent event) {
        return new EventData(toKey(event.type, event.part))
                .withData(VALUE, MetaDataType.STRING_TO_DOUBLE, event.value);
    }

    private static BreweryKey toKey(BodyPartChangeType bodyPartChangeType, @Nullable BodyPart bodyPart) {
        String bodyPartName = bodyPart == null ? "all" : bodyPart.name().toLowerCase(Locale.ROOT);
        return new BreweryKey("bodyhealth", bodyPartChangeType.name().toLowerCase(Locale.ROOT) + "_" + bodyPartName);
    }

    @Override
    public String getId() {
        return "bodyhealth";
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("bodyhealth.api.BodyHealthAPI");
    }

    public enum BodyPartChangeType {
        SET_HEALTH,
        DAMAGE,
        HEAL
    }

    public record BodyHealthEvent(BodyPartChangeType type, @Nullable BodyPart part, double value, boolean percent,
                                  boolean force) implements IntegrationEvent {
        @Override
        public void run(Holder.Player player) {
            BukkitAdapter.toPlayer(player)
                    .ifPresent(bukkitPlayer -> {
                        if (part == null) {
                            Arrays.stream(BodyPart.values()).forEach(bodyPart -> changeHealth(bodyPart, bukkitPlayer));
                        } else {
                            changeHealth(part, bukkitPlayer);
                        }
                    });
        }

        private void changeHealth(BodyPart part, Player player) {
            BodyHealthAPI api = BodyHealthAPI.getInstance();
            double partMaxHealth = api.getMaxPartHealth(player, part);
            switch (type) {
                case SET_HEALTH -> api.setHealth(player, part, percent ? value : value * 100 / partMaxHealth);
                case DAMAGE ->
                        api.damagePlayerDirectly(player, percent ? value * partMaxHealth / 100 : value, part, force);
                case HEAL ->
                        api.healPlayer(player, part, (int) Math.round(percent ? value * partMaxHealth / 100 : value), force);
            }
        }

        @Override
        public BreweryKey key() {
            return toKey(type, part);
        }

        @Override
        public Component displayName() {
            String name = type.name().toLowerCase(Locale.ROOT) + " " + (part == null ? "all" : part.name().toLowerCase(Locale.ROOT));
            return Component.text(name.replace("_", " "));
        }

        @Override
        public EventProbability probability() {
            return EventProbability.NONE;
        }
    }
}
