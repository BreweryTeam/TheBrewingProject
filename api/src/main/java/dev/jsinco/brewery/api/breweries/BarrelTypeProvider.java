package dev.jsinco.brewery.api.breweries;

public interface BarrelTypeProvider {

    BarrelType.Builder builder(String name);

    BarrelType predefined(String name);


    static BarrelType.Builder builderStatic(String name) {
        return BarrelTypeProviderHolder.instance().builder(name);
    }

    static BarrelType predefinedStatic(String name) {
        return BarrelTypeProviderHolder.instance().predefined(name);
    }
}
