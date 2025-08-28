package dev.jsinco.brewery.api.util;

import org.jetbrains.annotations.Contract;


/**
 * @param a The first value in the tuple
 * @param b The second value in the tuple
 */
public record Pair<A, B>(A a, B b) {

    /**
     * Gets the first value in the tuple
     */
    @Contract(pure = true)
    public A first() {
        return a;
    }

    /**
     * Gets the second value in the tuple
     */
    @Contract(pure = true)
    public B second() {
        return b;
    }

    /**
     * Returns a pair with the same value for both a and b
     *
     * @param val The value to set a and b to
     * @param <K> The type of the value
     * @return A pair with the same value for both a and b
     */
    public static <K> Pair<K, K> singleValue(K val) {
        return new Pair<>(val, val);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pair<?, ?> pair)) {
            return false;
        }

        return pair.a().equals(this.a()) && pair.b().equals(this.b());
    }

    @Override
    public int hashCode() {
        return a.hashCode() ^ b.hashCode();
    }

    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}