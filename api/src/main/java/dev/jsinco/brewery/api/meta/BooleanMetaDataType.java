package dev.jsinco.brewery.api.meta;

/**
 * A convenience type for boolean metadata, stored internally as a byte, where 0 is false and everything else is true.
 */
public class BooleanMetaDataType implements MetaDataType<Byte, Boolean> {

    public static final BooleanMetaDataType INSTANCE = new BooleanMetaDataType();

    @Override
    public Class<Byte> getPrimitiveType() {
        return Byte.class;
    }

    @Override
    public Class<Boolean> getComplexType() {
        return Boolean.class;
    }

    @Override
    public Byte toPrimitive(Boolean complex) {
        return complex ? (byte) 1 : (byte) 0;
    }

    @Override
    public Boolean toComplex(Byte primitive) {
        return primitive != 0;
    }

}
