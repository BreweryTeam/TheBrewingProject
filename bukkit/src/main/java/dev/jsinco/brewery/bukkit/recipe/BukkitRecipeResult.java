package dev.jsinco.brewery.bukkit.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.translation.GlobalTranslator;
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

    @Override
    public ItemStack newBrewItem(@NotNull BrewScore score, @NotNull Brew brew, @NotNull Brew.State state) {
        ItemStack itemStack = newLorelessItem();
        applyLore(itemStack, score, brew, state);
        return itemStack;
    }

    @Override
    public ItemStack newLorelessItem() {
        if (customId != null) {
            ItemStack itemStack = createCustomItem();

            if (itemStack != null) {
                applyData(itemStack);
                return itemStack;
            }
            Logger.logErr("Invalid item id '" + customId + "' for recipe: " + name);
        }
        ItemStack itemStack = new ItemStack(Material.POTION);
        applyData(itemStack);
        return itemStack;
    }

    @Override
    public Component displayName() {
        return MiniMessage.miniMessage().deserialize(name);
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
                    .getIntegrations(IntegrationTypes.ITEM)
                    .stream()
                    .filter(Integration::isEnabled)
                    .filter(integration -> customId.namespace().equals(integration.getId()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Namespace should be within the supported items plugins"))
                    .createItem(customId.key())
                    .orElse(null);
        }
    }

    private void applyData(ItemStack itemStack) {
        BrewAdapter.hideTooltips(itemStack);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, MessageUtil.miniMessage(name)
                .decoration(TextDecoration.ITALIC, false)
                .colorIfAbsent(NamedTextColor.WHITE)
        );
        if (glint) {
            itemStack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments().add(Enchantment.MENDING, 1));
        }
        if (customModelData > 0) {
            itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(customModelData).build());
        }
        if (itemModel != null) {
            itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModel);
        }
        recipeEffects.applyTo(itemStack);
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                .customColor(color)
        );
    }

    private void applyLore(ItemStack itemStack, BrewScore score, Brew brew, Brew.State state) {
        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(
                Stream.concat(lore.stream()
                                        .map(line -> MessageUtil.miniMessage(line, MessageUtil.getScoreTagResolver(score))),
                                compileExtraLore(score, brew, state)
                        )
                        .map(component -> component.decoration(TextDecoration.ITALIC, false))
                        .map(component -> component.colorIfAbsent(NamedTextColor.GRAY))
                        .map(component -> GlobalTranslator.render(component, Config.config().language()))
                        .toList()
        ));
    }

    private Stream<? extends Component> compileExtraLore(BrewScore score, Brew brew, Brew.State state) {
        if (!appendBrewInfoLore) {
            return Stream.empty();
        }
        Stream.Builder<Component> streamBuilder = Stream.builder();
        streamBuilder.add(Component.empty());
        TagResolver resolver = getResolver(score);
        switch (state) {
            case Brew.State.Brewing ignored -> {
                streamBuilder.add(Component.translatable("tbp.brew.tooltip.quality-brewing", Argument.tagResolver(resolver)));
                MessageUtil.compileBrewInfo(brew, score, false).forEach(streamBuilder::add);
                applyDrunkenTooltips(state, streamBuilder, resolver);
            }
            case Brew.State.Other ignored -> {
                streamBuilder.add(Component.translatable("tbp.brew.tooltip.quality", Argument.tagResolver(resolver)));
                addLastStepLore(brew, streamBuilder, score, state);
                applyDrunkenTooltips(state, streamBuilder, resolver);
            }
            case Brew.State.Seal seal -> {
                if (seal.message() != null) {
                    streamBuilder.add(Component.translatable("tbp.brew.tooltip.volume", Argument.tagResolver(
                            TagResolver.resolver(resolver, Placeholder.parsed("volume", seal.message())))
                    ));
                }
                streamBuilder.add(Component.translatable("tbp.brew.tooltip.quality-sealed", Argument.tagResolver(resolver)));
                addLastStepLore(brew, streamBuilder, score, state);
                applyDrunkenTooltips(state, streamBuilder, resolver);
            }
        }
        return streamBuilder.build();
    }

    private void addLastStepLore(Brew brew, Stream.Builder<Component> streamBuilder, BrewScore score, Brew.State state) {
        int lastIndex = brew.getCompletedSteps().size() - 1;
        BrewingStep lastCompleted = brew.lastCompletedStep();
        streamBuilder.add(lastCompleted.infoDisplay(state, MessageUtil.getBrewStepTagResolver(lastCompleted, score.getPartialScores(lastIndex), score.brewDifficulty())));
    }

    private void applyDrunkenTooltips(Brew.State state, Stream.Builder<Component> streamBuilder, TagResolver resolver) {
        DrunkenModifierSection.modifiers().drunkenTooltips()
                .stream()
                .filter(modifierTooltip -> modifierTooltip.filter().evaluate(DrunkStateImpl.compileVariables(recipeEffects.getModifiers(), null, 0D)) > 0)
                .map(modifierTooltip -> modifierTooltip.getTooltip(state))
                .filter(Objects::nonNull)
                .map(miniMessage -> MessageUtil.miniMessage(miniMessage, resolver))
                .forEach(streamBuilder::add);
    }

    private @NotNull TagResolver getResolver(BrewScore score) {
        TagResolver.Builder output = TagResolver.builder();
        output.resolvers(
                MessageUtil.numberedModifierTagResolver(recipeEffects.getModifiers(), null),
                MessageUtil.getScoreTagResolver(score)
        );
        return output.build();
    }


    public static class Builder implements dev.jsinco.brewery.api.util.Builder<RecipeResult<ItemStack>> {

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
            BreweryKey namespacedKey = BreweryKey.parse(customId, "minecraft");
            List<String> ids = TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationTypes.ITEM)
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
            Objects.requireNonNull(recipeEffects, "Effects not initialized, a recipe has to have effects");
            if(lore == null) {
                lore = List.of();
            }
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
