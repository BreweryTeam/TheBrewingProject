package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.effect.ModifierConsumePdcType;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.persistence.PersistentDataContainerView;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class RecipeEffects {

    public static final NamespacedKey COMMANDS = TheBrewingProject.key("commands");
    public static final NamespacedKey MESSAGE = TheBrewingProject.key("message");
    public static final NamespacedKey ACTION_BAR = TheBrewingProject.key("action_bar");
    public static final NamespacedKey TITLE = TheBrewingProject.key("titles");
    public static final NamespacedKey ALCOHOL = TheBrewingProject.key("alcohol");
    public static final NamespacedKey TOXINS = TheBrewingProject.key("toxins");
    public static final NamespacedKey MODIFIERS = TheBrewingProject.key("modifiers");
    public static final NamespacedKey EVENTS = TheBrewingProject.key("events");
    public static final NamespacedKey EFFECTS = TheBrewingProject.key("effects");
    private static final List<NamespacedKey> PDC_TYPES = List.of(COMMANDS, MESSAGE, ACTION_BAR, TITLE, ALCOHOL, TOXINS, EVENTS);

    public static final RecipeEffects GENERIC = new Builder()
            .effects(List.of())
            .build();

    private final @NotNull List<@NotNull RecipeEffect> effects;
    private final @Nullable String title;
    private final @Nullable String message;
    private final @Nullable String actionBar;
    private final @NotNull Map<DrunkenModifier, Double> modifiers;
    private final @NotNull List<@NotNull BreweryKey> events;

    private RecipeEffects(@NotNull List<RecipeEffect> effects, @Nullable String title, @Nullable String message,
                          @Nullable String actionBar, @NotNull List<@NotNull BreweryKey> events, Map<DrunkenModifier, Double> modifiers) {
        this.effects = effects;
        this.title = title;
        this.message = message;
        this.actionBar = actionBar;
        this.modifiers = modifiers;
        this.events = events;
    }

    public List<DrunkEvent> getEvents() {
        CustomEventRegistry customEventRegistry = TheBrewingProject.getInstance().getCustomDrunkEventRegistry();
        return events
                .stream()
                .map(eventKey -> {
                    if (BreweryRegistry.DRUNK_EVENT.containsKey(eventKey)) {
                        return BreweryRegistry.DRUNK_EVENT.get(eventKey);
                    } else {
                        return customEventRegistry.getCustomEvent(eventKey);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void applyTo(ItemStack itemStack) {
        itemStack.editPersistentDataContainer(this::applyTo);
    }

    private void applyTo(PersistentDataContainer container) {
        PDC_TYPES.forEach(container::remove);
        if (title != null) {
            container.set(TITLE, PersistentDataType.STRING, title);
        }
        if (message != null) {
            container.set(MESSAGE, PersistentDataType.STRING, message);
        }
        if (actionBar != null) {
            container.set(ACTION_BAR, PersistentDataType.STRING, actionBar);
        }
        container.set(EVENTS, ListPersistentDataType.STRING_LIST, events.stream().map(BreweryKey::toString).toList());
        container.set(EFFECTS, RecipeEffectPersistentDataType.INSTANCE, effects);
    }

    public static Optional<RecipeEffects> fromEntity(Entity entity) {
        return fromPdc(entity.getPersistentDataContainer());
    }

    private static Optional<RecipeEffects> fromPdc(PersistentDataContainerView persistentDataContainer) {
        if (!persistentDataContainer.has(ALCOHOL)) {
            return Optional.empty();
        }
        RecipeEffects.Builder builder = new RecipeEffects.Builder();
        Map<DrunkenModifier, Double> modifiers = new HashMap<>();
        DrunkenModifierSection.modifiers()
                .optionalModifier("alcohol")
                .filter(modifier -> persistentDataContainer.has(ALCOHOL, PersistentDataType.INTEGER))
                .ifPresent(modifier -> modifiers.put(modifier, (double) persistentDataContainer.get(ALCOHOL, PersistentDataType.INTEGER)));
        DrunkenModifierSection.modifiers()
                .optionalModifier("toxins")
                .filter(modifier -> persistentDataContainer.has(TOXINS, PersistentDataType.INTEGER))
                .ifPresent(modifier -> modifiers.put(modifier, (double) persistentDataContainer.get(TOXINS, PersistentDataType.INTEGER)));
        if (persistentDataContainer.has(TITLE)) {
            builder.title(persistentDataContainer.get(TITLE, PersistentDataType.STRING));
        }
        if (persistentDataContainer.has(MESSAGE)) {
            builder.message(persistentDataContainer.get(MESSAGE, PersistentDataType.STRING));
        }
        if (persistentDataContainer.has(ACTION_BAR)) {
            builder.actionBar(persistentDataContainer.get(ACTION_BAR, PersistentDataType.STRING));
        }
        List<ModifierConsume> consumeModifiers = persistentDataContainer.get(MODIFIERS, ModifierConsumePdcType.LIST_INSTANCE);
        if (consumeModifiers != null) {
            consumeModifiers.forEach(modifierConsume -> modifiers.put(modifierConsume.modifier(), modifierConsume.value()));
        }
        builder.events(persistentDataContainer.getOrDefault(EVENTS, ListPersistentDataType.STRING_LIST, List.of())
                .stream()
                .map(BreweryKey::parse)
                .collect(Collectors.toList())
        );
        if (persistentDataContainer.has(EFFECTS, RecipeEffectPersistentDataType.INSTANCE)) {
            builder.effects(persistentDataContainer.get(EFFECTS, RecipeEffectPersistentDataType.INSTANCE));
        }
        return Optional.of(builder.build());
    }

    public static Optional<RecipeEffects> fromItem(@NotNull ItemStack item) {
        return fromPdc(item.getPersistentDataContainer());
    }

    public void applyTo(Player player) {
        DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        if (!player.hasPermission("brewery.override.drunk")) {
            drunksManager.consume(player.getUniqueId(),
                    modifiers.entrySet().stream()
                            .map(entry -> new ModifierConsume(entry.getKey(), entry.getValue(), true))
                            .toList()
            );
        }
        if (title != null) {
            player.showTitle(Title.title(
                    MessageUtil.miniMessage(title,
                            BukkitMessageUtil.getPlayerTagResolver(player),
                            MessageUtil.numberedModifierTagResolver(modifiers, "consumed")
                    ),
                    Component.empty())
            );
        }
        if (message != null) {
            player.sendMessage(MessageUtil.miniMessage(message,
                    BukkitMessageUtil.getPlayerTagResolver(player),
                    MessageUtil.numberedModifierTagResolver(modifiers, "consumed")
            ));
        }
        if (actionBar != null) {
            player.sendActionBar(MessageUtil.miniMessage(actionBar,
                    BukkitMessageUtil.getPlayerTagResolver(player),
                    MessageUtil.numberedModifierTagResolver(modifiers, "consumed")
            ));
        } else {
            player.sendActionBar(
                    Component.translatable("tbp.info.after-drink",
                            Argument.tagResolver(MessageUtil.getDrunkStateTagResolver(drunksManager.getDrunkState(player.getUniqueId())))
                    )
            );
        }
        if (player.hasPermission("brewery.override.effect")) {
            return;
        }
        getEvents().forEach(drunkEvent -> TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvent(player.getUniqueId(), drunkEvent));
        getEffects().stream()
                .map(RecipeEffect::newPotionEffect)
                .forEach(player::addPotionEffect);
    }

    public void applyTo(Projectile projectile) {
        PersistentDataContainer persistentDataContainer = projectile.getPersistentDataContainer();
        applyTo(persistentDataContainer);
    }

    public static class Builder {

        private List<RecipeEffect> effects = List.of();
        private @Nullable String title;
        private @Nullable String message;
        private @Nullable String actionBar;
        private @NotNull List<@NotNull BreweryKey> events = List.of();
        private ImmutableMap.Builder<DrunkenModifier, Double> modifiers = new ImmutableMap.Builder<>();

        public Builder effects(@NotNull List<@NotNull RecipeEffect> effects) {
            Preconditions.checkNotNull(effects);
            this.effects = effects;
            return this;
        }

        public Builder events(@NotNull List<@NotNull BreweryKey> events) {
            Preconditions.checkNotNull(events);
            this.events = List.copyOf(events);
            return this;
        }

        public Builder title(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder message(@Nullable String message) {
            this.message = message;
            return this;
        }

        public Builder actionBar(@Nullable String actionBar) {
            this.actionBar = actionBar;
            return this;
        }

        public Builder addModifiers(Map<DrunkenModifier, Double> modifiers) {
            this.modifiers.putAll(modifiers);
            return this;
        }

        public RecipeEffects build() {
            return new RecipeEffects(effects, title, message, actionBar, events, modifiers.build());
        }

    }
}
