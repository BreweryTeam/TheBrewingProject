package dev.jsinco.brewery.bukkit.testutil;

import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.jspecify.annotations.NonNull;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;

import java.util.function.Consumer;

public class ItemStackMockPDC extends ItemStackMock {
    public ItemStackMockPDC(Material material) {
        super(material);
    }

    @Override
    public boolean editPersistentDataContainer(@NonNull Consumer<PersistentDataContainer> consumer) {
        return editMeta(meta -> consumer.accept(meta.getPersistentDataContainer()));
    }
}
