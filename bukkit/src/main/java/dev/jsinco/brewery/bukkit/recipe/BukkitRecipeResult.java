package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewScore;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.integration.Integration;
import dev.jsinco.brewery.bukkit.integration.IntegrationType;
import dev.jsinco.brewery.bukkit.integration.ItemIntegration;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.recipe.RecipeResult;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Logger;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class BukkitRecipeResult implements RecipeResult<ItemStack> {

    public static final @NotNull RecipeResult<ItemStack> GENERIC = new Builder()
            .lore(List.of())
            .name("Generic")
            .recipeEffects(RecipeEffects.GENERIC)
            .build();
    private final boolean glint;
    private final int customModelData;
    private final @Nullable NamespacedKey itemModel;
    private final @Nullable BreweryKey customId;

    @Getter
    private final String name;
    @Getter
    private final List<String> lore;

    @Getter
    private final RecipeEffects recipeEffects;
    @Getter
    private final Color color;
    private final boolean appendBrewInfoLore;

    private BukkitRecipeResult(boolean glint, int customModelData, @Nullable NamespacedKey itemModel, RecipeEffects recipeEffects, String name, List<String> lore, Color color, boolean appendBrewInfoLore, @Nullable BreweryKey customId) {
        this.glint = glint;
        this.customModelData = customModelData;
        this.itemModel = itemModel;
        this.recipeEffects = recipeEffects;
        this.name = name;
        this.lore = lore;
        this.color = color;
        this.appendBrewInfoLore = appendBrewInfoLore;
        this.customId = customId;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ItemStack newBrewItem(@NotNull BrewScore score, @NotNull Brew brew, @NotNull Brew.State state) {
        if (customId != null) {
            ItemStack itemStack = createCustomItem();
            if (itemStack != null) {
                applyData(itemStack, score, brew, state);
                return itemStack;
            }
            Logger.logErr("Invalid item id '" + customId + "' for recipe: " + name);
        }
        ItemStack itemStack = new ItemStack(Material.POTION);
        applyData(itemStack, score, brew, state);
        return itemStack;
    }

    private @Nullable ItemStack createCustomItem() {
        if (customId.namespace().equals("minecraft")) {
            ItemType itemType = Registry.ITEM.get(BukkitAdapter.toNamespacedKey(customId));
            if (itemType == null || itemType == ItemType.AIR) {
                return null;
            }
            return itemType.createItemStack();
        } else {
            return TheBrewingProject.getInstance().getIntegrationManager().getIntegrationRegistry()
                    .getIntegrations(IntegrationType.ITEM)
                    .stream()
                    .filter(Integration::enabled)
                    .filter(integration -> customId.namespace().equals(integration.getId()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Namespace should be within the supported items plugins"))
                    .createItem(customId.key())
                    .orElse(null);
        }
    }

    private void applyData(ItemStack itemStack, BrewScore score, Brew brew, Brew.State state) {
        BrewAdapter.hideTooltips(itemStack);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, compileMessage(score, brew, name, true).decoration(TextDecoration.ITALIC, false));
        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(
                Stream.concat(lore.stream()
                                        .map(line -> compileMessage(score, brew, line, false)),
                                compileExtraLore(score, brew, state)
                        )
                        .map(component -> component.decoration(TextDecoration.ITALIC, false))
                        .map(component -> component.colorIfAbsent(NamedTextColor.GRAY))
                        .toList()
        ));
        if (glint) {
            itemStack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments().add(Enchantment.MENDING, 1));
        }
        if (customModelData > 0) {
            itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(customModelData).build());
        }
        if (itemModel != null) {
            itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModel);
        }
        recipeEffects.withToxins(recipeEffects, (int) (recipeEffects.getAlcohol() * (1.5D - score.score()))).applyTo(itemStack);
        // After recipe effects, as both might modify potion contents
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                .addCustomEffects(itemStack.getData(DataComponentTypes.POTION_CONTENTS).customEffects())
                .customColor(color)
        );
    }

    private Stream<? extends Component> compileExtraLore(BrewScore score, Brew brew, Brew.State state) {
        if (!appendBrewInfoLore) {
            return Stream.empty();
        }
        Stream.Builder<Component> streamBuilder = Stream.builder();
        streamBuilder.add(Component.empty());
        switch (state) {
            case Brew.State.Brewing ignored -> {
                streamBuilder.add(compileMessage(score, brew, TranslationsConfig.BREW_TOOLTIP_QUALITY_BREWING, false));
                MessageUtil.compileBrewInfo(brew, score, false).forEach(streamBuilder::add);
                int alcohol = recipeEffects.getAlcohol();
                if (alcohol > 0) {
                    streamBuilder.add(MessageUtil.miniMessage(TranslationsConfig.DETAILED_ALCOHOLIC, Formatter.number("alcohol", alcohol)));
                }
            }
            case Brew.State.Other ignored -> {
                streamBuilder.add(compileMessage(score, brew, TranslationsConfig.BREW_TOOLTIP_QUALITY, false));
                addLastStepLore(brew, streamBuilder, score, false);
            }
            case Brew.State.Seal seal -> {
                if (seal.message() != null) {
                    streamBuilder.add(MessageUtil.miniMessage(TranslationsConfig.BREW_TOOLTIP_VOLUME, Placeholder.parsed("volume", seal.message())));
                }
                streamBuilder.add(MessageUtil.miniMessage(TranslationsConfig.BREW_TOOLTIP_QUALITY_SEALED, MessageUtil.getScoreTagResolver(score)));
                addLastStepLore(brew, streamBuilder, score, true);
            }
        }
        return streamBuilder.build();
    }

    private void addLastStepLore(Brew brew, Stream.Builder<Component> streamBuilder, BrewScore score, boolean sealed) {
        Map<String, String> lore = sealed ? TranslationsConfig.BREW_TOOLTIP_SEALED : TranslationsConfig.BREW_TOOLTIP;
        BrewingStep brewingStep = brew.lastCompletedStep();
        streamBuilder.add(MessageUtil.miniMessage(
                lore.get(brewingStep.stepType().name().toLowerCase(Locale.ROOT)),
                MessageUtil.getBrewStepTagResolver(brewingStep, score.getPartialScores(brew.getCompletedSteps().size() - 1), score.brewDifficulty()))
        );
        if (recipeEffects.getAlcohol() > 0) {
            streamBuilder.add(MessageUtil.miniMessage(TranslationsConfig.ALCOHOLIC));
        }
    }

    private Component compileMessage(BrewScore score, Brew brew, String serializedMiniMessage, boolean isBrewName) {
        return MessageUtil.miniMessage(serializedMiniMessage, getResolver(score, brew, isBrewName));
    }

    private @NotNull TagResolver getResolver(BrewScore score, Brew brew, boolean isBrewName) {
        TagResolver.Builder output = TagResolver.builder();
        if (!isBrewName) {
            output.resolver(Placeholder.component("brew_name", compileMessage(score, brew, this.name, true)));
        }
        output.resolvers(
                Formatter.number("alcohol", this.getRecipeEffects().getAlcohol()),
                MessageUtil.getScoreTagResolver(score)
        );

        return output.build();
    }


    public static class Builder {

        private boolean glint;
        private int customModelData;
        private NamespacedKey itemModel;
        private String name;
        private List<String> lore;
        private RecipeEffects recipeEffects;
        private Color color = Color.BLUE;
        private boolean appendBrewInfoLore = true;
        private BreweryKey customId;

        public Builder glint(boolean glint) {
            this.glint = glint;
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder itemModel(@Nullable String itemModel) {
            if (itemModel != null) {
                this.itemModel = NamespacedKey.fromString(itemModel);
                if (this.itemModel == null) {
                    throw new IllegalArgumentException("Illegal namespaced key");
                }
            }
            return this;
        }

        public Builder recipeEffects(@NotNull RecipeEffects recipeEffects) {
            this.recipeEffects = Objects.requireNonNull(recipeEffects);
            return this;
        }

        public Builder color(@NotNull Color color) {
            this.color = color;
            return this;
        }

        public Builder appendBrewInfoLore(boolean appendBrewInfoLore) {
            this.appendBrewInfoLore = appendBrewInfoLore;
            return this;
        }

        public Builder customId(@Nullable String customId) {
            if (customId == null) {
                this.customId = null;
                return this;
            }
            BreweryKey namespacedKey = BreweryKey.parse(customId.toLowerCase(Locale.ROOT), "minecraft");
            List<String> ids = TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationType.ITEM)
                    .stream().map(ItemIntegration::getId)
                    .toList();
            if (ids.contains(namespacedKey.namespace()) || "minecraft".equals(namespacedKey.namespace())) {
                this.customId = namespacedKey;
                return this;
            }
            throw new IllegalArgumentException("Unknown key, can not identify namespace: " + namespacedKey);
        }

        public BukkitRecipeResult build() {
            Objects.requireNonNull(name, "Names not initialized, a recipe has to have names");
            Objects.requireNonNull(lore, "Lore not initialized, a recipe has to have lore");
            Objects.requireNonNull(recipeEffects, "Effects not initialized, a recipe has to have effects");
            return new BukkitRecipeResult(glint, customModelData, itemModel, recipeEffects, name, lore, color, appendBrewInfoLore, customId);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lore(List<String> lore) {
            this.lore = lore;
            return this;
        }
    }
}
