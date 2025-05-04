package dev.jsinco.brewery.util;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Registry<T extends BreweryKeyed> {

    public static final Registry<BarrelType> BARREL_TYPE = fromEnums(BarrelType.class);
    public static final Registry<CauldronType> CAULDRON_TYPE = fromEnums(CauldronType.class);
    public static final Registry<StructureMeta<?>> STRUCTURE_META = (Registry<StructureMeta<?>>) fromFields(StructureMeta.class);
    public static final Registry<StructureType> STRUCTURE_TYPE = (Registry<StructureType>) fromFields(StructureType.class);
    public static final Registry<NamedDrunkEvent> DRUNK_EVENT = fromEnums(NamedDrunkEvent.class);

    private final ImmutableMap<BreweryKey, T> backing;

    private Registry(List<T> values) {
        ImmutableMap.Builder<BreweryKey, T> registryBacking = ImmutableMap.builder();
        values.forEach(value -> registryBacking.put(value.key(), value));
        this.backing = registryBacking.build();
    }

    public Collection<T> values() {
        return backing.values();
    }

    public @Nullable T get(BreweryKey key) {
        return backing.get(key);
    }

    public boolean containsKey(BreweryKey key) {
        return backing.containsKey(key);
    }

    private static <E extends Enum<E> & BreweryKeyed> Registry<E> fromEnums(Class<E> enumClass) {
        return new Registry<>(Arrays.stream(enumClass.getEnumConstants()).toList());
    }

    private static <T extends BreweryKeyed> Registry<? extends T> fromFields(Class<T> tClass) {
        try {
            List<T> tList = new ArrayList<>();
            for (Field field : tClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object staticField = field.get(null);
                if (tClass.isInstance(staticField)) {
                    tList.add(tClass.cast(staticField));
                }
            }
            return new Registry<>(tList);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
