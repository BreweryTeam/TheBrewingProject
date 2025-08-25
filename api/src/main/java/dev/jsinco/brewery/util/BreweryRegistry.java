package dev.jsinco.brewery.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Static registry access for the brewing project
 *
 * @param <T> The Keyed Type
 */
public class BreweryRegistry<T extends BreweryKeyed> {

    public static final BreweryRegistry<BarrelType> BARREL_TYPE = fromEnums(BarrelType.class);
    public static final BreweryRegistry<CauldronType> CAULDRON_TYPE = fromEnums(CauldronType.class);
    public static final BreweryRegistry<StructureMeta<?>> STRUCTURE_META = (BreweryRegistry<StructureMeta<?>>) fromFields(StructureMeta.class);
    public static final BreweryRegistry<StructureType> STRUCTURE_TYPE = (BreweryRegistry<StructureType>) fromFields(StructureType.class);
    public static final BreweryRegistry<NamedDrunkEvent> DRUNK_EVENT = fromJson("/named_drunk_events.json", NamedDrunkEvent.class);

    private final ImmutableMap<BreweryKey, T> backing;

    private BreweryRegistry(Collection<T> values) {
        ImmutableMap.Builder<BreweryKey, T> registryBacking = ImmutableMap.builder();
        values.forEach(value -> registryBacking.put(value.key(), value));
        this.backing = registryBacking.build();
    }

    /**
     * @return All keyed values for this registry
     */
    public Collection<T> values() {
        return backing.values();
    }

    /**
     * @param key A brewery key
     * @return The brewery keyed object
     */
    public @Nullable T get(BreweryKey key) {
        return backing.get(key);
    }

    /**
     * @param key The brewery key
     * @return True if registry contains key
     */
    public boolean containsKey(BreweryKey key) {
        return backing.containsKey(key);
    }

    private static <E extends Enum<E> & BreweryKeyed> BreweryRegistry<E> fromEnums(Class<E> enumClass) {
        return new BreweryRegistry<>(Arrays.stream(enumClass.getEnumConstants()).toList());
    }

    private static <T extends BreweryKeyed> BreweryRegistry<? extends T> fromFields(Class<T> tClass) {
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
            return new BreweryRegistry<>(tList);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private static <T extends BreweryKeyed> BreweryRegistry<T> fromJson(String path, Class<T> tClass) {
        Gson gson = new Gson(); // Make this static if this method is called multiple times

        try (
                InputStream inputStream = BreweryRegistry.class.getResourceAsStream(path);
                InputStreamReader reader = new InputStreamReader(
                        Preconditions.checkNotNull(inputStream, "InputStream for path '" + path + "' cannot be null")
                )
        ) {
            Type listType = TypeToken.getParameterized(List.class, tClass).getType();
            List<T> tList = gson.fromJson(reader, listType);
            return new BreweryRegistry<>(tList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON at path: " + path, e);
        }
    }
}
