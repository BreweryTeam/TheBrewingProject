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

public class Registry<T extends BreweryKeyed> {

    public static final Registry<BarrelType> BARREL_TYPE = fromEnums(BarrelType.class);
    public static final Registry<CauldronType> CAULDRON_TYPE = fromEnums(CauldronType.class);
    public static final Registry<StructureMeta<?>> STRUCTURE_META = (Registry<StructureMeta<?>>) fromFields(StructureMeta.class);
    public static final Registry<StructureType> STRUCTURE_TYPE = (Registry<StructureType>) fromFields(StructureType.class);
    public static final Registry<NamedDrunkEvent> DRUNK_EVENT = fromJson("/named_drunk_events.json", NamedDrunkEvent.class);

    private final ImmutableMap<BreweryKey, T> backing;

    private Registry(Collection<T> values) {
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


    public static <T extends BreweryKeyed> Registry<T> fromJson(String path, Class<T> tClass) {
        Gson gson = new Gson(); // Make this static if this method is called multiple times

        try (
                InputStream inputStream = Registry.class.getResourceAsStream(path);
                InputStreamReader reader = new InputStreamReader(
                        Preconditions.checkNotNull(inputStream, "InputStream for path '" + path + "' cannot be null")
                )
        ) {
            Type listType = TypeToken.getParameterized(List.class, tClass).getType();
            List<T> tList = gson.fromJson(reader, listType);
            return new Registry<>(tList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON at path: " + path, e);
        }
    }
}
