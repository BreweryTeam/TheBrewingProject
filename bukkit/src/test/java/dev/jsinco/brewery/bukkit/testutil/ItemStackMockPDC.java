package dev.jsinco.brewery.bukkit.testutil;

import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;

import java.util.function.Consumer;

public class ItemStackMockPDC extends ItemStackMock {
    public ItemStackMockPDC(Material material) {
        super(material);
    }

    @Override
    public boolean editPersistentDataContainer(@NotNull Consumer<PersistentDataContainer> consumer) {
        return editMeta(meta -> consumer.accept(meta.getPersistentDataContainer()));
    }
}
