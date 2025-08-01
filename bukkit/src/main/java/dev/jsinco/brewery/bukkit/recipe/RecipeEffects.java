package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.bukkit.util.ListPersistentDataType;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.util.Registry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.persistence.PersistentDataContainerView;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class RecipeEffects {

    public static final NamespacedKey COMMANDS = BukkitAdapter.toNamespacedKey(BreweryKey.parse("commands"));
    public static final NamespacedKey MESSAGE = BukkitAdapter.toNamespacedKey(BreweryKey.parse("message"));
    public static final NamespacedKey ACTION_BAR = BukkitAdapter.toNamespacedKey(BreweryKey.parse("action_bar"));
    public static final NamespacedKey TITLE = BukkitAdapter.toNamespacedKey(BreweryKey.parse("titles"));
    public static final NamespacedKey ALCOHOL = BukkitAdapter.toNamespacedKey(BreweryKey.parse("alcohol"));
    public static final NamespacedKey TOXINS = BukkitAdapter.toNamespacedKey(BreweryKey.parse("toxins"));
    public static final NamespacedKey EVENTS = BukkitAdapter.toNamespacedKey(BreweryKey.parse("events"));
    private static final List<NamespacedKey> PDC_TYPES = List.of(COMMANDS, MESSAGE, ACTION_BAR, TITLE, ALCOHOL, TOXINS, EVENTS);

    public static final RecipeEffects GENERIC = new Builder()
            .effects(List.of())
            .build();

    private final @NotNull List<@NotNull RecipeEffect> effects;
    private final @Nullable String title;
    private final @Nullable String message;
    private final @Nullable String actionBar;
    private final int alcohol;
    private final @NotNull List<@NotNull BreweryKey> events;
    private final int toxins;

    private RecipeEffects(@NotNull List<RecipeEffect> effects, @Nullable String title, @Nullable String message, @Nullable String actionBar, int alcohol, @NotNull List<@NotNull BreweryKey> events, int toxins) {
        this.effects = effects;
        this.title = title;
        this.message = message;
        this.actionBar = actionBar;
        this.alcohol = alcohol;
        this.events = events;
        this.toxins = toxins;
    }

    public List<DrunkEvent> getEvents() {
        CustomEventRegistry customEventRegistry = TheBrewingProject.getInstance().getCustomDrunkEventRegistry();
        return events
                .stream()
                .map(eventKey -> {
                    if (Registry.DRUNK_EVENT.containsKey(eventKey)) {
                        return Registry.DRUNK_EVENT.get(eventKey);
                    } else {
                        return customEventRegistry.getCustomEvent(eventKey);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void applyTo(ItemStack itemStack) {
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().addCustomEffects(
                effects.stream().map(RecipeEffect::newPotionEffect).toList()
        ).build());
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
        container.set(ALCOHOL, PersistentDataType.INTEGER, alcohol);
        container.set(TOXINS, PersistentDataType.INTEGER, toxins);
        container.set(EVENTS, ListPersistentDataType.STRING_LIST, events.stream().map(BreweryKey::toString).toList());
    }

    public static Optional<RecipeEffects> fromEntity(Entity entity) {
        return fromPdc(entity.getPersistentDataContainer());
    }

    private static Optional<RecipeEffects> fromPdc(PersistentDataContainerView persistentDataContainer) {
        if (!persistentDataContainer.has(ALCOHOL)) {
            return Optional.empty();
        }
        RecipeEffects.Builder builder = new RecipeEffects.Builder();
        builder.alcohol(persistentDataContainer.getOrDefault(ALCOHOL, PersistentDataType.INTEGER, 0));
        builder.toxins(persistentDataContainer.getOrDefault(TOXINS, PersistentDataType.INTEGER, 0));
        if (persistentDataContainer.has(TITLE)) {
            builder.title(persistentDataContainer.get(TITLE, PersistentDataType.STRING));
        }
        if (persistentDataContainer.has(MESSAGE)) {
            builder.message(persistentDataContainer.get(MESSAGE, PersistentDataType.STRING));
        }
        if (persistentDataContainer.has(ACTION_BAR)) {
            builder.actionBar(persistentDataContainer.get(ACTION_BAR, PersistentDataType.STRING));
        }
        builder.events(persistentDataContainer.getOrDefault(EVENTS, ListPersistentDataType.STRING_LIST, List.of())
                .stream()
                .map(BreweryKey::parse)
                .collect(Collectors.toList())
        );
        return Optional.of(builder.build());
    }

    public static Optional<RecipeEffects> fromItem(@NotNull ItemStack item) {
        return fromPdc(item.getPersistentDataContainer());
    }

    public void applyTo(Player player) {
        DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        drunksManager.consume(player.getUniqueId(), alcohol, toxins);
        if (title != null) {
            player.showTitle(Title.title(BukkitMessageUtil.compilePlayerMessage(title, player, drunksManager, this.alcohol), Component.empty()));
        }
        if (message != null) {
            player.sendMessage(BukkitMessageUtil.compilePlayerMessage(message, player, drunksManager, this.alcohol));
        }
        if (actionBar != null) {
            player.sendActionBar(BukkitMessageUtil.compilePlayerMessage(actionBar, player, drunksManager, this.alcohol));
        } else {
            player.sendActionBar(
                    MessageUtil.miniMessage(TranslationsConfig.INFO_AFTER_DRINK,
                            MessageUtil.getDrunkStateTagResolver(drunksManager.getDrunkState(player.getUniqueId()))
                    )
            );
        }
        TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(player.getUniqueId(), getEvents().stream().map(EventStep.class::cast).toList());
    }

    public void applyTo(Projectile projectile) {
        PersistentDataContainer persistentDataContainer = projectile.getPersistentDataContainer();
        applyTo(persistentDataContainer);
    }

    public RecipeEffects withToxins(RecipeEffects recipeEffects, int toxins) {
        return new RecipeEffects(recipeEffects.effects, recipeEffects.title, recipeEffects.message,
                recipeEffects.actionBar, recipeEffects.alcohol, recipeEffects.events, toxins);
    }

    public static class Builder {

        private List<RecipeEffect> effects = List.of();
        private @Nullable String title;
        private @Nullable String message;
        private @Nullable String actionBar;
        private @NotNull List<@NotNull BreweryKey> events = List.of();
        private int alcohol;
        private int toxins;

        public Builder effects(@NotNull List<@NotNull RecipeEffect> effects) {
            this.effects = effects;
            return this;
        }

        public Builder events(@NotNull List<@NotNull BreweryKey> events) {
            this.events = events;
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

        public Builder alcohol(int alcohol) {
            this.alcohol = alcohol;
            return this;
        }

        public Builder toxins(int toxins) {
            this.toxins = toxins;
            return this;
        }

        public RecipeEffects build() {
            Preconditions.checkNotNull(effects);
            Preconditions.checkNotNull(events);
            return new RecipeEffects(effects, title, message, actionBar, alcohol, events, toxins);
        }

    }
}
