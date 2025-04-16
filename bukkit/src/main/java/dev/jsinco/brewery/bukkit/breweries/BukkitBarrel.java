package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.Barrel;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.Tickable;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.brew.BukkitBarrelBrewDataType;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.util.moment.Interval;
import dev.jsinco.brewery.util.moment.Moment;
import dev.jsinco.brewery.util.vector.BreweryLocation;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

@Getter
public class BukkitBarrel implements Barrel<BukkitBarrel, ItemStack, Inventory>, Tickable, InventoryHolder {
    private final PlacedBreweryStructure<BukkitBarrel> structure;
    @Getter
    private final @NotNull Inventory inventory;
    @Getter
    private final int size;
    @Getter
    private final BarrelType type;
    @Getter
    private final Location signLocation;
    private Brew[] brews;

    public BukkitBarrel(Location signLocation, PlacedBreweryStructure<BukkitBarrel> structure, int size, BarrelType type) {
        this.structure = structure;
        this.size = size;
        this.inventory = Bukkit.createInventory(this, size, "Barrel");
        this.type = type;
        this.brews = new Brew[size];
        this.signLocation = signLocation;
    }

    public boolean open(@NotNull BreweryLocation location, @NotNull UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (!player.hasPermission("brewery.barrel.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.BARREL_ACCESS_DENIED));
            return true;
        }
        if (inventory.getViewers().isEmpty()) {
            populateInventory();
        }
        float randPitch = (float) (Math.random() * 0.1);
        if (signLocation != null) {
            signLocation.getWorld().playSound(signLocation, Sound.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
        }
        player.openInventory(inventory);
        return true;
    }

    @Override
    public boolean inventoryAllows(@NotNull UUID playerUuid, @NotNull ItemStack item) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return false;
        }
        if (!player.hasPermission("brewery.barrel.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.BARREL_ACCESS_DENIED));
            return false;
        }
        return BrewAdapter.fromItem(item).isPresent();
    }

    @Override
    public Set<Inventory> getInventories() {
        return Set.of(this.inventory);
    }

    private void close() {
        float randPitch = (float) (Math.random() * 0.1);
        if (signLocation != null) {
            signLocation.getWorld().playSound(signLocation, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 0.8f + randPitch);
        }
        this.inventory.clear();
    }

    @Override
    public void tick() {
        updateInventory();
        if (inventory.getViewers().isEmpty()) {
            close();
            TheBrewingProject.getInstance().getBreweryRegistry().unregisterOpened(this);
        }
    }

    @Override
    public void destroy() {
        // TODO: What should be done when this barrel is destroyed? Probably drop all the brews, right?
    }

    @Override
    public PlacedBreweryStructure<BukkitBarrel> getStructure() {
        return structure;
    }

    private void populateInventory() {
        for (int i = 0; i < brews.length; i++) {
            if (brews[i] == null) {
                continue;
            }
            ItemStack brewItem = BrewAdapter.toItem(brews[i], Brew.State.BREWING);
            this.inventory.setItem(i, brewItem);
        }
    }

    private void updateInventory() {
        ItemStack[] inventoryContents = getInventory().getContents();
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack itemStack = inventoryContents[i];
            if (itemStack == null) {
                if (brews[i] != null) {
                    try {
                        TheBrewingProject.getInstance().getDatabase().remove(BukkitBarrelBrewDataType.INSTANCE, new Pair<>(brews[i], getContext(i)));
                    } catch (PersistenceException e) {
                        e.printStackTrace();
                    }
                }
                brews[i] = null;
                continue;
            }
            Optional<Brew> brewOptional = BrewAdapter.fromItem(itemStack);
            final int iFinal = i;
            brewOptional.ifPresent(brew -> {
                long time = TheBrewingProject.getInstance().getTime();
                if (!(brew.lastStep() instanceof BrewingStep.Age age) || age.barrelType() != type) {
                    brew = brew.withStep(new BrewingStep.Age(new Interval(time, time), type));
                }
                if (Objects.equals(brew, brews[iFinal])) {
                    brews[iFinal] = brew.witModifiedLastStep(brewStep -> {
                        BrewingStep.Age ageBrewStep = ((BrewingStep.Age) brewStep);
                        Moment moment = ageBrewStep.age();
                        Interval interval = moment instanceof Interval interval1 ? interval1.withLastStep(time) : new Interval(time - moment.moment(), time);
                        return ageBrewStep.withAge(interval);
                    });
                    inventory.setItem(iFinal, BrewAdapter.toItem(brews[iFinal], Brew.State.BREWING));
                    return;
                }
                brew = brew.witModifiedLastStep(
                        brewingStep -> {
                            BrewingStep.Age ageBrewStep = ((BrewingStep.Age) brewingStep);
                            return ageBrewStep.withAge(ageBrewStep.age().withMovedEnding(time));
                        }
                );
                inventory.setItem(iFinal, BrewAdapter.toItem(brew, Brew.State.BREWING));
                Database database = TheBrewingProject.getInstance().getDatabase();
                BukkitBarrelBrewDataType.BarrelContext context = getContext(iFinal);
                try {
                    if (brews[iFinal] == null) {
                        database.insertValue(BukkitBarrelBrewDataType.INSTANCE, new Pair<>(brew, context));
                    } else {
                        database.updateValue(BukkitBarrelBrewDataType.INSTANCE, new Pair<>(brew, context));
                    }
                } catch (PersistenceException e) {
                    e.printStackTrace();
                }
                brews[iFinal] = brew;
            });
        }
    }

    private BukkitBarrelBrewDataType.BarrelContext getContext(int inventoryPos) {
        return new BukkitBarrelBrewDataType.BarrelContext(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ(), inventoryPos, signLocation.getWorld().getUID());
    }

    World getWorld() {
        return signLocation.getWorld();
    }

    public void setBrews(List<Pair<Brew, Integer>> brews) {
        this.brews = new Brew[size];
        for (Pair<Brew, Integer> brew : brews) {
            this.brews[brew.second()] = brew.first();
        }
    }

    public List<Pair<Brew, Integer>> getBrews() {
        List<Pair<Brew, Integer>> brewList = new ArrayList<>();
        for (int i = 0; i < brews.length; i++) {
            if (brews[i] == null) {
                continue;
            }
            brewList.add(new Pair<>(brews[i], i));
        }
        return brewList;
    }
}
