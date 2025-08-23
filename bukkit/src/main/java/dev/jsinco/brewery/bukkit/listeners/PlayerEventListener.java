package dev.jsinco.brewery.bukkit.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.breweries.InventoryAccessible;
import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType;
import dev.jsinco.brewery.bukkit.effect.event.DrunkEventExecutor;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.integration.IntegrationType;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.serializers.ConsumableSerializer;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.effect.text.DrunkTextRegistry;
import dev.jsinco.brewery.effect.text.DrunkTextTransformer;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.ScoredIngredient;
import dev.jsinco.brewery.recipes.RecipeRegistryImpl;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.util.Logger;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
        Optional<StructureHolder<?>> possibleStructureHolder = placedStructureRegistry.getHolder(BukkitAdapter.toBreweryLocation(playerInteractEvent.getClickedBlock().getLocation()));
        if (possibleStructureHolder.isEmpty()) {
            return;
        }

        if (!TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationType.STRUCTURE)
                .stream()
                .map(structureIntegration -> structureIntegration.hasAccess(playerInteractEvent.getClickedBlock(), playerInteractEvent.getPlayer()))
                .reduce(true, Boolean::logicalAnd)) {
            String structureType = possibleStructureHolder.get().getStructureType().key().key().toLowerCase();
            MessageUtil.message(playerInteractEvent.getPlayer(), "tbp." + structureType + ".access-denied");
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
        if (!TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationType.STRUCTURE)
                .stream()
                .map(structureIntegration -> structureIntegration.hasAccess(event.getClickedBlock(), event.getPlayer()))
                .reduce(true, Boolean::logicalAnd)) {
            // send something to the player here?
            return;
        }
        if (Tag.CAULDRONS.isTagged(block.getType())) {
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
        if (itemStack.getType() == Material.GLASS_BOTTLE) {
            cauldronOptional
                    .map(BukkitCauldron::extractBrew)
                    .ifPresent(brewItemStack -> {
                        updateHeldItem(decreaseItem(itemStack, event.getPlayer()), event.getPlayer(), event.getHand());
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), brewItemStack);
                        if (BukkitCauldron.decrementLevel(block)) {
                            ListenerUtil.removeActiveSinglePositionStructure(cauldronOptional.get(), breweryRegistry, database);
                        }
                    });
        }
        cauldronOptional
                .filter(cauldron -> itemStack.getType() == Material.CLOCK)
                .filter(cauldron -> event.getPlayer().hasPermission("brewery.cauldron.time"))
                .ifPresent(cauldron -> Component.translatable("tbp.cauldron.clock-message", Argument.tagResolver(MessageUtil.getTimeTagResolver(cauldron.getTime()))));
        cauldronOptional.ifPresent(ignored -> {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        });
    }

    private boolean handleIngredientAddition(ItemStack itemStack, Block block, @Nullable BukkitCauldron cauldron, Player player, @Nullable EquipmentSlot hand) {
        if (block.getType() == Material.CAULDRON && itemStack.getType() != Material.POTION) {
            return false;
        }
        if (cauldron == null) {
            cauldron = this.initCauldron(block);
        }
        boolean addedIngredient = cauldron.addIngredient(itemStack, player);
        if (addedIngredient) {
            updateHeldItem(decreaseItem(itemStack, player), player, hand);
            try {
                database.updateValue(BukkitCauldronDataType.INSTANCE, cauldron);
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

    private BukkitCauldron initCauldron(Block block) {
        BukkitCauldron newCauldron = new BukkitCauldron(BukkitAdapter.toBreweryLocation(block), BukkitCauldron.isHeatSource(block.getRelative(BlockFace.DOWN)));
        try {
            database.insertValue(BukkitCauldronDataType.INSTANCE, newCauldron);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
        breweryRegistry.addActiveSinglePositionStructure(newCauldron);
        return newCauldron;
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
        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(itemStack);
        if (ingredient instanceof ScoredIngredient scoredIngredient) {
            ingredient = scoredIngredient.baseIngredient();
        }
        return recipeRegistry.isRegisteredIngredient(ingredient);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        DrunkStateImpl drunkState = drunksManager.getDrunkState(playerUuid);
        if (drunkState == null) {
            return;
        }
        String text = event.getMessage();
        String transformed = DrunkTextTransformer.transform(text, drunkTextRegistry, drunkState.alcohol());
        event.setMessage(transformed);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        RecipeEffects.fromItem(event.getItem())
                .ifPresent(effect -> effect.applyTo(event.getPlayer()));
        Ingredient ingredient = BukkitIngredientManager.INSTANCE.getIngredient(event.getItem());
        for (ConsumableSerializer.Consumable consumable : Config.config().decayRate().consumables()) {
            String key = consumable.type().contains(":") ? consumable.type() : "minecraft:" + consumable.type();
            if (ingredient.getKey().equalsIgnoreCase(key)) {
                TheBrewingProject.getInstance().getDrunksManager()
                        .consume(event.getPlayer().getUniqueId(), consumable.alcohol(), consumable.toxins());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        drunkEventExecutor.onPlayerJoin(event.getPlayer().getUniqueId());
        drunksManager.planEvent(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerProjectileLaunch(PlayerLaunchProjectileEvent event) {
        RecipeEffects.fromItem(event.getItemStack())
                .ifPresent(recipeEffects -> recipeEffects.applyTo(event.getProjectile()));
    }
}
