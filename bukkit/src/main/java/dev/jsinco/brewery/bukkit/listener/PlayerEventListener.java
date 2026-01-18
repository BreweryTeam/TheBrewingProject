package dev.jsinco.brewery.bukkit.listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.api.breweries.StructureHolder;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.ModifierDisplay;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.BrewConsumeEvent;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType;
import dev.jsinco.brewery.bukkit.effect.ConsumedModifierDisplay;
import dev.jsinco.brewery.bukkit.effect.event.DrunkEventExecutor;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffectsImpl;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.configuration.serializers.ConsumableSerializer;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.effect.text.DrunkTextRegistry;
import dev.jsinco.brewery.effect.text.DrunkTextTransformer;
import dev.jsinco.brewery.format.TimeFormat;
import dev.jsinco.brewery.format.TimeFormatter;
import dev.jsinco.brewery.format.TimeModifier;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerEventListener implements Listener {
    public static final Set<Material> DISALLOWED_INGREDIENT_MATERIALS = Set.of(Material.CLOCK, Material.BUCKET, Material.GLASS_BOTTLE);
    private static final Random RANDOM = new Random();

    private final PlacedStructureRegistryImpl placedStructureRegistry;
    private final BreweryRegistry breweryRegistry;
    private final Database database;
    private final DrunksManagerImpl<?> drunksManager;
    private final DrunkTextRegistry drunkTextRegistry;
    private final RecipeRegistryImpl<ItemStack> recipeRegistry;
    private final DrunkEventExecutor drunkEventExecutor;

    public PlayerEventListener(PlacedStructureRegistryImpl placedStructureRegistry, BreweryRegistry breweryRegistry, Database database, DrunksManagerImpl<?> drunksManager, DrunkTextRegistry drunkTextRegistry, RecipeRegistryImpl<ItemStack> recipeRegistry, DrunkEventExecutor drunkEventExecutor) {
        this.placedStructureRegistry = placedStructureRegistry;
        this.breweryRegistry = breweryRegistry;
        this.database = database;
        this.drunksManager = drunksManager;
        this.drunkTextRegistry = drunkTextRegistry;
        this.recipeRegistry = recipeRegistry;
        this.drunkEventExecutor = drunkEventExecutor;
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractStructure(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK || playerInteractEvent.getPlayer().isSneaking() || playerInteractEvent.getHand() != EquipmentSlot.HAND) {
            return;
        }
        BreweryLocation location = BukkitAdapter.toBreweryLocation(playerInteractEvent.getClickedBlock().getLocation());
        Optional<StructureHolder<?>> possibleStructureHolder = placedStructureRegistry.getHolder(location);
        if (possibleStructureHolder.isEmpty()) {
            return;
        }
        if (!TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationTypes.STRUCTURE)
                .stream()
                .map(structureIntegration -> structureIntegration.hasAccess(playerInteractEvent.getClickedBlock(), playerInteractEvent.getPlayer(), possibleStructureHolder.get().getStructureType().key()))
                .reduce(true, Boolean::logicalAnd)) {
            MessageUtil.message(playerInteractEvent.getPlayer(), "tbp." + possibleStructureHolder.get().getStructureType().key().key().toLowerCase() + ".access-denied");
            return;
        }
        if (possibleStructureHolder.get() instanceof InventoryAccessible<?, ?> inventoryAccessible
                && inventoryAccessible.open(BukkitAdapter.toBreweryLocation(playerInteractEvent.getClickedBlock()), playerInteractEvent.getPlayer().getUniqueId())) {
            breweryRegistry.registerOpened((InventoryAccessible<ItemStack, Inventory>) inventoryAccessible);
        }
        playerInteractEvent.setUseItemInHand(Event.Result.DENY);
        playerInteractEvent.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useItemInHand() == Event.Result.DENY || !event.hasItem()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        boolean cauldron = Tag.CAULDRONS.isTagged(block.getType());
        if (!TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationTypes.STRUCTURE)
                .stream()
                .map(structureIntegration -> structureIntegration.hasAccess(event.getClickedBlock(), event.getPlayer(),
                        cauldron ? BreweryKey.parse("cauldron") : BreweryKey.parse("sealing_table")))
                .reduce(true, Boolean::logicalAnd)) {
            if (cauldron && breweryRegistry
                    .getActiveSinglePositionStructure(BukkitAdapter.toBreweryLocation(block))
                    .filter(BukkitCauldron.class::isInstance)
                    .map(BukkitCauldron.class::cast)
                    .isPresent() // still allow accessing vanilla cauldrons
            ) {
                MessageUtil.message(event.getPlayer(), "tbp.cauldron.access-denied");
                event.setCancelled(true);
            }
            return;
        }

        if (cauldron) {
            handleCauldron(event, block);
        }

        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack offHand = inventory.getItemInOffHand();
        if (block.getType() == Material.CRAFTING_TABLE && offHand.getType() == Material.PAPER && event.getPlayer().

                isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack mainHand = inventory.getItemInMainHand();
            ItemStack sealed = BrewAdapter.fromItem(mainHand)
                    .map(brew -> BrewAdapter.toItem(brew, new BrewImpl.State.Seal(offHand.hasData(DataComponentTypes.CUSTOM_NAME) ? MiniMessage.miniMessage().serialize(offHand.getData(DataComponentTypes.CUSTOM_NAME)) : null)))
                    .orElse(mainHand);
            inventory.setItemInMainHand(sealed);
            event.setUseItemInHand(Event.Result.DENY);
            decreaseItem(offHand, event.getPlayer());
            inventory.setItemInOffHand(offHand);
        }
        if (block.getType() == Material.HOPPER && event.getItem() != null) {
            PersistentDataContainerView view = event.getItem().getPersistentDataContainer();
            Double score = view.get(BrewAdapter.BREWERY_SCORE, PersistentDataType.DOUBLE);
            if ((Config.config().emptyAnyDrinkUsingHopper() || (score != null && score == 0))
                    && BrewAdapter.fromItem(event.getItem()).isPresent() && event.getItem().getType() == Material.POTION) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                ItemStack emptyBottle = new ItemStack(Material.GLASS_BOTTLE);
                if (!event.getPlayer().getInventory().addItem(emptyBottle).isEmpty()) {
                    event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), emptyBottle);
                }
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                SoundPlayer.playSoundEffect(Config.config().sounds().emptyFailedDrink(), Sound.Source.BLOCK, block.getLocation().toCenterLocation());
            }
        }

    }

    private ItemStack decreaseItem(ItemStack itemStack, Player player) {
        if (player.getGameMode() == GameMode.CREATIVE && !Config.config().consumeItemsInCreative()) {
            return itemStack;
        }
        if (itemStack.getType() == Material.POTION) {
            return new ItemStack(Material.GLASS_BOTTLE);
        }
        if (itemStack.getType() == Material.MILK_BUCKET) {
            return new ItemStack(Material.BUCKET);
        }
        itemStack.setAmount(itemStack.getAmount() - 1);
        return itemStack;
    }

    private void handleCauldron(PlayerInteractEvent event, @NotNull Block block) {
        Optional<BukkitCauldron> cauldronOptional = breweryRegistry.getActiveSinglePositionStructure(BukkitAdapter.toBreweryLocation(block))
                .filter(BukkitCauldron.class::isInstance)
                .map(BukkitCauldron.class::cast);
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
        if (isIngredient(itemStack)) {
            boolean addedIngredient = handleIngredientAddition(itemStack, block, cauldronOptional.orElse(null), event.getPlayer(), event.getHand());
            if (addedIngredient) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
        if (cauldronOptional.isEmpty()) {
            return;
        }
        BukkitCauldron cauldron = cauldronOptional.get();
        if (itemStack.getType() == Material.GLASS_BOTTLE) {
            handleCauldronExtract(event, block, cauldron);
        }
        if (itemStack.getType() == Material.CLOCK && event.getPlayer().hasPermission("brewery.cauldron.time")) {
            event.getPlayer().sendMessage(
                    Component.translatable("tbp.cauldron.clock-message", Argument.tagResolver(
                            Placeholder.parsed("time", TimeFormatter.format(cauldron.getTime(), TimeFormat.CLOCK_MECHANIC, TimeModifier.COOKING))
                    ))
            );
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
    }

    private void handleCauldronExtract(PlayerInteractEvent event, Block block, BukkitCauldron cauldron) {
        Player player = event.getPlayer();
        Brew brew = cauldron.getUpdatedBrew();

        CauldronExtractEvent extractEvent = new CauldronExtractEvent(
                cauldron,
                new ItemSource.BrewBasedSource(brew, new Brew.State.Other()),
                player.hasPermission("brewery.cauldron.access") ?
                        new CancelState.Allowed() : new CancelState.PermissionDenied(Component.translatable("tbp.cauldron.access-denied")),
                player
        );
        if (!extractEvent.callEvent()) {
            if (extractEvent.getCancelState() instanceof CancelState.PermissionDenied(Component denyMessage)) {
                player.sendMessage(denyMessage);
            }
            return;
        }

        cauldron.extractBrew();
        ItemStack brewItemStack = extractEvent.getBrewSource().get();
        updateHeldItem(decreaseItem(event.getItem(), player), player, event.getHand());
        player.getWorld().dropItem(player.getLocation(), brewItemStack);
        if (BukkitCauldron.decrementLevel(block)) {
            ListenerUtil.removeActiveSinglePositionStructure(cauldron, breweryRegistry, database);
        }
    }

    private boolean handleIngredientAddition(ItemStack itemStack, Block block, @Nullable BukkitCauldron cauldron, Player player, @Nullable EquipmentSlot hand) {
        if (block.getType() == Material.CAULDRON && itemStack.getType() != Material.POTION) {
            return false;
        }
        boolean createNewCauldron = cauldron == null;
        if (createNewCauldron) {
            cauldron = new BukkitCauldron(BukkitAdapter.toBreweryLocation(block), BukkitCauldron.isHeatSource(block.getRelative(BlockFace.DOWN)));
        }
        boolean addedIngredient = cauldron.addIngredient(itemStack, player);
        if (addedIngredient) {
            updateHeldItem(decreaseItem(itemStack, player), player, hand);
            try {
                if (createNewCauldron) {
                    database.insertValue(BukkitCauldronDataType.INSTANCE, cauldron);
                    breweryRegistry.addActiveSinglePositionStructure(cauldron);
                } else {
                    database.updateValue(BukkitCauldronDataType.INSTANCE, cauldron);
                }
            } catch (PersistenceException e) {
                Logger.logErr(e);
            }
        }
        return addedIngredient;
    }

    private void updateHeldItem(ItemStack item, Player player, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(item);
        } else if (equipmentSlot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else {
            throw new IllegalArgumentException("Only main hand and offhand equipment slots are allowed: " + equipmentSlot);
        }
    }


    private boolean isIngredient(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        Material type = itemStack.getType();
        if (DISALLOWED_INGREDIENT_MATERIALS.contains(type)) {
            return false;
        }
        if (Config.config().allowUnregisteredIngredients()) {
            return true;
        }
        return recipeRegistry.isRegisteredIngredient(BukkitIngredientManager.INSTANCE.getIngredient(itemStack));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        DrunkState drunkState = drunksManager.getDrunkState(playerUuid);
        if (drunkState == null) {
            return;
        }
        String text = event.getMessage();
        String transformed = DrunkTextTransformer.transform(text, drunkTextRegistry, drunkState.recalculate(TheBrewingProject.getInstance().getTime()));
        event.setMessage(transformed);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Optional<RecipeEffectsImpl> effects = RecipeEffectsImpl.fromItem(event.getItem());
        if (effects.isPresent()) {
            BrewConsumeEvent consumeEvent = new BrewConsumeEvent(event.getPlayer(), event.getItem(), event.getHand(), event.getReplacement());
            if (!consumeEvent.callEvent()) {
                event.setCancelled(true);
                return;
            }
            effects.get().applyTo(event.getPlayer());
            event.setReplacement(consumeEvent.getReplacement());
        }

        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(event.getItem());
        for (ConsumableSerializer.Consumable consumable : DrunkenModifierSection.modifiers().consumables()) {
            String key = consumable.type().contains(":") ? consumable.type() : "minecraft:" + consumable.type();
            if (ingredient.getKey().equalsIgnoreCase(key)) {
                List<ModifierConsume> consumedModifiers = consumable.modifiers().entrySet().stream()
                        .map(entry -> new ModifierConsume(DrunkenModifierSection.modifiers().modifier(entry.getKey()), entry.getValue(), true))
                        .toList();
                DrunkState beforeState = drunksManager.getDrunkState(event.getPlayer().getUniqueId());
                DrunkState afterState = drunksManager.consume(event.getPlayer().getUniqueId(), consumedModifiers);
                for (ModifierDisplay.DisplayWindow window : ModifierDisplay.DisplayWindow.values()) {
                    ConsumedModifierDisplay.renderConsumeDisplay(event.getPlayer(), window, beforeState, afterState, consumedModifiers);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        drunkEventExecutor.onPlayerJoinServer(event.getPlayer().getUniqueId());
        drunkEventExecutor.onPlayerJoinWorld(event.getPlayer().getUniqueId(), event.getPlayer().getWorld());
        drunksManager.planEvent(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        drunkEventExecutor.onPlayerJoinWorld(event.getPlayer().getUniqueId(), event.getPlayer().getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        drunkEventExecutor.onDeathExecutions(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            drunkEventExecutor.onDamage(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerProjectileLaunch(PlayerLaunchProjectileEvent event) {
        RecipeEffectsImpl.fromItem(event.getItemStack())
                .ifPresent(recipeEffects -> recipeEffects.applyTo(event.getProjectile()));
    }
}
