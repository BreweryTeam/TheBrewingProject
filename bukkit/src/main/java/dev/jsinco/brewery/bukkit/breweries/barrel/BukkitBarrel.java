package dev.jsinco.brewery.bukkit.breweries.barrel;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.Barrel;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.structure.PlacedBreweryStructure;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.SoundPlayer;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.Pair;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class BukkitBarrel implements Barrel<BukkitBarrel, ItemStack, Inventory> {
    private final PlacedBreweryStructure<BukkitBarrel> structure;
    @Getter
    private final int size;
    @Getter
    private final BarrelType type;
    @Getter
    private final Location uniqueLocation;
    private final BrewInventory inventory;
    private long recentlyAccessed = -1L;

    public BukkitBarrel(Location uniqueLocation, @NotNull PlacedBreweryStructure<BukkitBarrel> structure, int size, @NotNull BarrelType type) {
        this.structure = Preconditions.checkNotNull(structure);
        this.size = size;
        this.type = Preconditions.checkNotNull(type);
        this.uniqueLocation = Preconditions.checkNotNull(uniqueLocation);
        this.inventory = new BrewInventory("Barrel", size, new BarrelBrewPersistenceHandler(BukkitAdapter.toBreweryLocation(uniqueLocation)));
    }

    @Override
    public boolean open(@NotNull BreweryLocation location, @NotNull UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (!player.hasPermission("brewery.barrel.access")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.BARREL_ACCESS_DENIED));
            return true;
        }
        if (inventoryUnpopulated()) {
            inventory.updateInventoryFromBrews();
        }
        recentlyAccessed = TheBrewingProject.getInstance().getTime();
        if (uniqueLocation != null) {
            SoundPlayer.playSoundEffect(Config.config().sounds.barrelOpen, Sound.Source.BLOCK, uniqueLocation.toCenterLocation());
        }
        player.openInventory(inventory.getInventory());
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
        return inventoryAllows(item);
    }

    @Override
    public boolean inventoryAllows(@NotNull ItemStack item) {
        return BrewAdapter.fromItem(item).isPresent();
    }

    @Override
    public Set<Inventory> getInventories() {
        return Set.of(this.inventory.getInventory());
    }

    private void close() {
        if (uniqueLocation != null) {
            SoundPlayer.playSoundEffect(Config.config().sounds.barrelClose, Sound.Source.BLOCK, uniqueLocation.toCenterLocation());
        }
        this.inventory.getInventory().clear();
    }

    @Override
    public void tickInventory() {
        Brew[] previousBrews = Arrays.copyOf(inventory.getBrews(), inventory.getBrews().length);
        inventory.updateBrewsFromInventory();
        if (!inventory.getInventory().getViewers().isEmpty()) {
            this.recentlyAccessed = TheBrewingProject.getInstance().getTime();
        }
        Brew[] brews = inventory.getBrews();
        long time = TheBrewingProject.getInstance().getTime();
        for (int i = 0; i < brews.length; i++) {
            Brew brew = brews[i];
            if (brew == null) {
                continue;
            }
            if (brew.lastStep() instanceof BrewingStep.Age age && age.barrelType() != type) {
                brew = brew.withStep(new AgeStepImpl(new Interval(time, time), this.type));
            }
            if (Objects.equals(previousBrews[i], brew)) {
                brews[i] = brew.withLastStep(BrewingStep.Age.class,
                        age -> age.withAge(age.time().withLastStep(time)),
                        () -> new AgeStepImpl(new Interval(time, time), this.type));
            } else {
                brews[i] = brew.withLastStep(BrewingStep.Age.class,
                        age -> age.withAge(age.time().withMovedEnding(time)),
                        () -> new AgeStepImpl(new Interval(time, time), this.type));
            }

        }
        inventory.updateInventoryFromBrews();
        if (inventoryUnpopulated()) {
            close();
            TheBrewingProject.getInstance().getBreweryRegistry().unregisterOpened(this);
        }
        if (time % 32 == 0) {
            getInventory().getInventory().getViewers()
                    .stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .forEach(Player::updateInventory);
        }
    }

    @Override
    public Optional<Inventory> access(@NotNull BreweryLocation breweryLocation) {
        if (inventoryUnpopulated()) {
            inventory.updateInventoryFromBrews();
            TheBrewingProject.getInstance().getBreweryRegistry().registerOpened(this);
        }
        this.recentlyAccessed = TheBrewingProject.getInstance().getTime();
        return Optional.of(inventory.getInventory());
    }

    @Override
    public void destroy(BreweryLocation breweryLocation) {
        Location location = BukkitAdapter.toLocation(breweryLocation).add(0.5, 0, 0.5);
        List<ItemStack> contents = inventory.destroy();
        contents.forEach(itemStack -> location.getWorld().dropItem(location, itemStack));
    }

    @Override
    public PlacedBreweryStructure<BukkitBarrel> getStructure() {
        return structure;
    }

    World getWorld() {
        return uniqueLocation.getWorld();
    }

    public List<Pair<Brew, Integer>> getBrews() {
        List<Pair<Brew, Integer>> brewList = new ArrayList<>();
        for (int i = 0; i < inventory.getBrews().length; i++) {
            if (inventory.getBrews()[i] == null) {
                continue;
            }
            brewList.add(new Pair<>(inventory.getBrews()[i], i));
        }
        return brewList;
    }

    private boolean inventoryUnpopulated() {
        return recentlyAccessed == -1L || recentlyAccessed + Moment.SECOND <= TheBrewingProject.getInstance().getTime();
    }
}
