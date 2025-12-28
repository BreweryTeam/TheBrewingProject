package dev.jsinco.brewery.api.meta;

import com.google.common.collect.Lists;

import java.util.List;

public class ListMetaDataType<P, C> implements MetaDataType<List<P>, List<C>> {

    private final MetaDataType<P, C> elementType;

    private ListMetaDataType(MetaDataType<P, C> elementType) {
        this.elementType = elementType;
    }

    public static <P, C> ListMetaDataType<P, C> from(MetaDataType<P, C> type) {
        return new ListMetaDataType<>(type);
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
