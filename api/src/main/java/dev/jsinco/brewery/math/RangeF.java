package dev.jsinco.brewery.math;

import java.util.Objects;

public class RangeF extends Range<Float> {
    public RangeF(Float min, Float max) {
        super(min, max);
    }

    public RangeF(String str) {
        super(str);
    }

    @Override
    protected void fromString(String str) {
        String[] parts = str.trim().split(";");
        if (parts.length == 0 || parts.length > 2) {
            throw new IllegalArgumentException("Invalid range");
        }

        this.setMin(Float.parseFloat(parts[0]));
        this.setMax(parts.length == 2 ? Float.parseFloat(parts[1]) : Float.parseFloat(parts[0]));
    }

    @Override
    public Float getRandom() {
        if (Objects.equals(this.getMin(), this.getMax())) {
            return this.getMax();
        }
        return RANDOM.nextFloat(this.getMin(), this.getMax());
    }
}
