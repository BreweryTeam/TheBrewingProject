package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.HolderProvider;
import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import org.bukkit.*;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class HolderProviderImpl implements HolderProvider {
    @Override
    public Optional<Holder.Material> material(BreweryKey materialKey) {
        return Optional.ofNullable(BukkitAdapter.toNamespacedKey(materialKey))
                .filter(key -> Registry.MATERIAL.get(key) != null)
                .map(key -> new Holder.Material(BukkitAdapter.toBreweryKey(key)));
    }

    @Override
    public Optional<Holder.Player> player(UUID playerUuid) {
        return Optional.ofNullable(Bukkit.getPlayer(playerUuid))
                .map(ignored -> new Holder.Player(playerUuid));
    }

    @Override
    public Optional<Holder.World> world(UUID worldUuid) {
        return Optional.ofNullable(Bukkit.getWorld(worldUuid))
                .map(ignored -> new Holder.World(worldUuid));
    }

    @Override
    public Set<Holder.Material> parseTag(BreweryKey tagKey) {
        NamespacedKey namespacedKey = BukkitAdapter.toNamespacedKey(tagKey);
        if (namespacedKey == null) {
            return Set.of();
        }
        Tag<Material> materialTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, namespacedKey, Material.class);
        return Optional.ofNullable(materialTag)
                .stream()
                .map(Tag::getValues)
                .flatMap(Collection::stream)
                .map(BukkitAdapter::toMaterialHolder)
                .collect(Collectors.toUnmodifiableSet());
    }
}
