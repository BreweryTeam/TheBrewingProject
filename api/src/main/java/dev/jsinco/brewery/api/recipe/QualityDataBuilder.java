package dev.jsinco.brewery.api.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.util.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class QualityDataBuilder<T, B extends Builder<T>> {

    private final QualityData<B> elementBuilders;

    public QualityDataBuilder(Supplier<B> elementBuilderSupplier) {
        this.elementBuilders = QualityData.fromValueMapper(ignored -> elementBuilderSupplier.get());
    }

    public QualityDataBuilder<T, B> addString(@NotNull String value, String errorMessage, BiConsumer<B, String> action) {
        Preconditions.checkNotNull(value, errorMessage);
        QualityData<String> stringQualityData = QualityData.readQualityFactoredString(value);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, stringQualityData.get(quality)));
        return this;
    }

    public <U> QualityDataBuilder<T, B> add(@NotNull String value, String errorMessage, Function<String, U> uMapper, BiConsumer<B, U> action) {
        Preconditions.checkNotNull(value, errorMessage);
        QualityData<U> uQualityData = QualityData.readQualityFactoredString(value)
                .map(uMapper);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, uQualityData.get(quality)));
        return this;
    }

    public QualityDataBuilder<T, B> addOptionalString(@Nullable String value, BiConsumer<B, String> action) {
        if (value == null) {
            return this;
        }
        QualityData<String> stringQualityData = QualityData.readQualityFactoredString(value);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, stringQualityData.get(quality)));
        return this;
    }

    public <U> QualityDataBuilder<T, B> addOptional(@Nullable String value, Function<String, U> uMapper, BiConsumer<B, U> action) {
        if (value == null) {
            return this;
        }
        QualityData<U> uQualityData = QualityData.readQualityFactoredString(value)
                .map(uMapper);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, uQualityData.get(quality)));
        return this;
    }

    public QualityDataBuilder<T, B> addStringList(@NotNull List<String> values, String errorMessage, BiConsumer<B, List<String>> action) {
        Preconditions.checkNotNull(values, errorMessage);
        QualityData<List<String>> uQualityData = QualityData.readQualityFactoredStringList(values);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, uQualityData.get(quality)));
        return this;
    }

    public QualityDataBuilder<T, B> addOptionalStringList(@Nullable List<String> values, BiConsumer<B, List<String>> action) {
        if (values == null) {
            return this;
        }
        QualityData<List<String>> uQualityData = QualityData.readQualityFactoredStringList(values);
        elementBuilders.forEach((quality, builder) -> action.accept(builder, uQualityData.get(quality)));
        return this;
    }

    public <U> QualityDataBuilder<T, B> add(QualityData<U> qualityData, BiConsumer<B, U> action) {
        Preconditions.checkNotNull(qualityData, "Quality data can not be null!");
        elementBuilders.forEach((quality, builder) -> action.accept(builder, qualityData.get(quality)));
        return this;
    }

    public QualityData<T> build() {
        return elementBuilders.map(Builder::build);
    }
}
