package dev.jsinco.brewery.configuration;

import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.api.structure.MaterialSubstitution;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class BarrelTypeDefinition extends OkaeriConfig implements BreweryKeyed {

    private BreweryKey key;

    private List<MaterialSubstitution> substitutions;

    private List<BarrelTypeRelation> relations;


    @Exclude
    public static final List<BarrelTypeDefinition> PREFABRICATED = List.of(
            new Builder("oak")
                    .barrelTypeRelations(
                            new BarrelTypeRelation("spruce", 0.9),
                            new BarrelTypeRelation("birch", 0.9),
                            new BarrelTypeRelation("jungle", 0.8),
                            new BarrelTypeRelation("pale_oak", 0.95),
                            new BarrelTypeRelation("dark_oak", 0.9)
                    )
                    .build(),
            new Builder("spruce")
                    .barrelTypeRelations(
                            new BarrelTypeRelation("dark_oak", 0.9)
                    ).materialSubstitutions(
                            new MaterialSubstitution("oak_planks", "spruce_planks"),
                            new MaterialSubstitution("oak_stairs", "spruce_stairs"),
                            new MaterialSubstitution("oak_wall_sign", "spruce_wall_sign")
                    ).build(),
            new Builder("birch")
                    .barrelTypeRelations(
                            new BarrelTypeRelation("cherry", 0.9)
                    ).materialSubstitutions(
                            new MaterialSubstitution("oak_planks", "birch_planks"),
                            new MaterialSubstitution("oak_stairs", "birch_stairs"),
                            new MaterialSubstitution("oak_wall_sign", "birch_wall_sign")
                    ).build(),
            new Builder("jungle")
                    .barrelTypeRelations(
                            new BarrelTypeRelation("acacia", 0.9),
                            new BarrelTypeRelation("mangrove", 0.9)
                    ).materialSubstitutions(
                            new MaterialSubstitution("oak_planks", "jungle_planks"),
                            new MaterialSubstitution("oak_stairs", "jungle_stairs"),
                            new MaterialSubstitution("oak_wall_sign", "jungle_wall_sign")
                    ).build(),
            new Builder("")
    );
    @Exclude
    public static final BarrelTypeDefinition ANY = compileAny(PREFABRICATED);

    private BarrelTypeDefinition(BreweryKey key, List<MaterialSubstitution> substitutions, List<BarrelTypeRelation> relations) {
        this.key = key;
        this.substitutions = substitutions;
        this.relations = relations;
    }

    private static BarrelTypeDefinition compileAny(List<BarrelTypeDefinition> prefabricated) {
        Builder builder = new BarrelTypeDefinition.Builder(BreweryKey.parse("any"));
        prefabricated.stream()
                .map(BarrelTypeDefinition::key)
                .forEach(key -> builder.barrelTypeRelation(key, 1D));
        return builder.build();
    }

    record BarrelTypeRelation(BreweryKey barrelType, double scoreRelation) {

        public BarrelTypeRelation(String barrelType, double scoreRelation) {
            this(BreweryKey.parse(barrelType), scoreRelation);
        }
    }

    static class Builder {

        private final BreweryKey key;
        private final ImmutableList.Builder<MaterialSubstitution> materialSubstitutionBuilder = ImmutableList.builder();
        private final ImmutableList.Builder<BarrelTypeRelation> barrelTypeRelationBuilder = ImmutableList.builder();

        public Builder(BreweryKey key) {
            this.key = key;
        }

        public Builder(String key) {
            this(BreweryKey.parse(key));
        }

        public Builder materialSubstitutions(MaterialSubstitution... materialSubstitution) {
            materialSubstitutionBuilder.add(materialSubstitution);
            return this;
        }

        public Builder barrelTypeRelations(BarrelTypeRelation... barrelTypeRelation) {
            barrelTypeRelationBuilder.add(barrelTypeRelation);
            return this;
        }

        public Builder barrelTypeRelation(BreweryKey barrelType, double score) {
            return barrelTypeRelations(new BarrelTypeRelation(barrelType, score));
        }

        public BarrelTypeDefinition build() {
            return new BarrelTypeDefinition(key, materialSubstitutionBuilder.build(), barrelTypeRelationBuilder.build());
        }
    }
}
