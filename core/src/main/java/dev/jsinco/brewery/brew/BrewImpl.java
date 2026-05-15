package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.util.BrewUtil;
import dev.jsinco.brewery.util.RegistryProviderHolder;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrewImpl implements Brew {

    private final List<BrewingStep> steps;
    private @Nullable List<List<BrewingStep>> variations = null;
    private final MetaData meta;
    public static final BrewSerializer SERIALIZER = new BrewSerializer();

    public BrewImpl(BrewingStep.Cook cook) {
        this(List.of(cook));
    }

    public BrewImpl(BrewingStep.Mix mix) {
        this(List.of(mix));
    }

    public BrewImpl(@NonNull List<BrewingStep> steps) {
        this(steps, new MetaData());
    }

    public BrewImpl(List<BrewingStep> steps, MetaData meta) {
        this.steps = steps;
        this.meta = meta;
    }

    @Override
    public BrewImpl withStep(BrewingStep step) {
        return new BrewImpl(Stream.concat(steps.stream().filter(BrewingStep::isCompleted), Stream.of(step)).toList(), meta);
    }

    @Override
    public BrewImpl withSteps(Collection<BrewingStep> steps) {
        return new BrewImpl(Stream.concat(this.steps.stream().filter(BrewingStep::isCompleted), steps.stream()).toList(), meta);
    }

    @Override
    public BrewImpl withStepsReplaced(Collection<BrewingStep> steps) {
        return new BrewImpl(List.copyOf(steps), meta);
    }

    @Override
    public BrewImpl withModifiedStep(int index, Function<BrewingStep, BrewingStep> modifier) {
        List<BrewingStep> brewingStepsList = new ArrayList<>(steps);
        brewingStepsList.set(index, modifier.apply(steps.get(index)));
        return new BrewImpl(
                Collections.unmodifiableList(brewingStepsList),
                meta
        );
    }

    @Override
    public BrewImpl witModifiedLastStep(Function<BrewingStep, BrewingStep> modifier) {
        BrewingStep newStep = modifier.apply(steps.getLast());
        return new BrewImpl(
                Stream.concat(
                        steps.subList(0, steps.size() - 1).stream(),
                        Stream.of(newStep)
                ).toList(),
                meta
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <B extends BrewingStep> Brew withModifiedLastStep(Class<B> bClass, Function<B, B> modifier) {
        if (!steps.isEmpty() && bClass.isInstance(lastStep())) {
            return witModifiedLastStep((Function<BrewingStep, BrewingStep>) modifier);
        }
        return this;
    }

    @Override
    public <B extends BrewingStep> BrewImpl withLastStep(Class<B> bClass, Function<B, B> modifier, Supplier<B> stepSupplier) {
        if (!steps.isEmpty() && bClass.isInstance(lastStep())) {
            BrewingStep newStep = modifier.apply(bClass.cast(lastStep()));
            return new BrewImpl(
                    Stream.concat(
                            steps.subList(0, steps.size() - 1).stream(),
                            Stream.of(newStep)
                    ).toList(),
                    meta
            );
        }
        return withStep(stepSupplier.get());
    }

    @Override
    public List<BrewingStep> getCompletedSteps() {
        return steps.stream()
                .filter(BrewingStep::isCompleted)
                .toList();
    }

    @Override
    public List<BrewingStep> getSteps() {
        return steps;
    }

    @Override
    public SequencedSet<UUID> getBrewers() {
        return steps.stream()
                .filter(BrewingStep::isCompleted)
                .map(BrewingStep::brewers)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Optional<UUID> leadBrewer() {
        Map<UUID, Integer> scores = new HashMap<>();
        steps.stream()
                .filter(BrewingStep::isCompleted)
                .map(BrewingStep::brewers)
                .flatMap(Collection::stream)
                .forEach(uuid -> scores.compute(uuid, (ignored, value) -> value == null ? 1 : value + 1));
        return scores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    @Override
    public int stepAmount() {
        return steps.size();
    }

    @Override
    public <P, C> Brew withMeta(Key key, MetaDataType<P, C> type, C value) {
        return new BrewImpl(steps, meta.withMeta(key, type, value));
    }

    @Override
    public Brew withoutMeta(Key key) {
        return new BrewImpl(steps, meta.withoutMeta(key));
    }

    @Override
    public MetaData meta() {
        return meta;
    }

    @Override
    public <P, C> @Nullable C meta(Key key, MetaDataType<P, C> type) {
        return meta.meta(key, type);
    }

    @Override
    public <P, C> boolean hasMeta(Key key, MetaDataType<P, C> type) {
        return meta.hasMeta(key, type);
    }

    @Override
    public Set<Key> metaKeys() {
        return meta.metaKeys();
    }

    @Override
    public List<List<BrewingStep>> variations() {
        if (this.variations == null) {
            this.variations = BrewUtil.variations(getCompletedSteps(), RegistryProviderHolder.instance().recipeRegistry());
        }
        return variations;
    }

    @Override
    public <I> Optional<Recipe<I>> closestRecipe(RecipeRegistry<I> registry) {
        List<Pair<BrewScore, Recipe<I>>> scores = variations()
                .stream()
                .flatMap(steps -> registry.closestRecipe(steps)
                        .map(recipe -> new Pair<>(recipe.score(steps), recipe))
                        .stream()
                )
                .toList();
        return scores.stream()
                .filter(pair -> pair.first().completed())
                .max(Comparator.comparingDouble(pair -> pair.first().rawScore()))
                .or(() -> scores.stream()
                        .max(Comparator.comparingDouble(pair -> pair.first().rawScore()))
                ).map(Pair::second);
    }

    @Override
    public @NonNull BrewScore score(Recipe<?> recipe) {
        return BrewUtil.variations(steps, RegistryProviderHolder.instance().recipeRegistry())
                .stream()
                .map(recipe::score)
                .max(Comparator.comparingDouble(BrewScore::score))
                .orElseGet(() -> recipe.score(steps));
    }

    @Override
    public Optional<BrewQuality> quality(Recipe<?> recipe) {
        return Optional.ofNullable(score(recipe).brewQuality());
    }

    @Override
    public @NonNull BrewingStep lastCompletedStep() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            BrewingStep step = steps.get(i);
            if (step.isCompleted()) {
                return step;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public @NonNull BrewingStep lastStep() {
        return steps.getLast();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BrewImpl brew = (BrewImpl) other;
        return steps.equals(brew.steps);
    }

    @Override
    public String toString() {
        return "BrewImpl{" +
                "steps=" + steps +
                ", meta=" + meta +
                '}';
    }

}
