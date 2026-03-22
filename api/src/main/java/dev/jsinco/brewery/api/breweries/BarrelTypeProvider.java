package dev.jsinco.brewery.api.breweries;

public interface BarrelTypeProvider {

    BarrelType.Builder builder(String name);

    BarrelType predefined(String name);
}
