package dev.jsinco.brewery.api.breweries;

import java.util.Collection;

public interface BarrelTypeProvider {

    BarrelType.Builder builder(String name);

    Collection<BarrelType> allBarrels();


    static BarrelType.Builder builderStatic(String name) {
        return BarrelTypeProviderHolder.instance().builder(name);
    }

    static Collection<BarrelType> allBarrelsStatic() {
        return BarrelTypeProviderHolder.instance().allBarrels();
    }
}
