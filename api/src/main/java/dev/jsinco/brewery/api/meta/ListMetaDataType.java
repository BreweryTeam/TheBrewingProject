package dev.jsinco.brewery.api.meta;

import com.google.common.collect.Lists;

import java.util.List;

public class ListMetaDataType<P, C> implements MetaDataType<List<P>, List<C>> {

    public static final int MAX_DEPTH = 10;

    private final MetaDataType<P, C> elementType;

    private ListMetaDataType(MetaDataType<P, C> elementType) {
        this.elementType = elementType;
    }

    /**
     * Creates a list type containing elements of the specified type.
     * @param elementType Type of the list element
     * @return A new list type
     * @param <P> The primitive Java type of the list elements
     * @param <C> The complex Java type of the list elements
     * @throws IllegalArgumentException when nesting list types deeper than {@link #MAX_DEPTH}
     */
    public static <P, C> ListMetaDataType<P, C> from(MetaDataType<P, C> elementType) {
        if (isTooDeeplyNested(elementType, 0)) {
            throw new IllegalArgumentException("Element type too deeply nested: " + elementType);
        }
        return new ListMetaDataType<>(elementType);
    }

    private static boolean isTooDeeplyNested(MetaDataType<?, ?> elementType, int depth) {
        if (depth > MAX_DEPTH) {
            return true;
        }
        if (elementType instanceof ListMetaDataType<?, ?> nestedListType) {
            return isTooDeeplyNested(nestedListType.elementType, depth + 1);
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<List<P>> getPrimitiveType() {
        return (Class<List<P>>) (Object) List.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<List<C>> getComplexType() {
        return (Class<List<C>>) (Object) List.class;
    }

    @Override
    public List<P> toPrimitive(List<C> complex) {
        return Lists.transform(complex, elementType::toPrimitive);
    }

    @Override
    public List<C> toComplex(List<P> primitive) {
        return Lists.transform(primitive, elementType::toComplex);
    }

    public MetaDataType<P, C> getElementDataType() {
        return elementType;
    }

}
