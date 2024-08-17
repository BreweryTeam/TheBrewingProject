package dev.jsinco.brewery.util;

import org.jetbrains.annotations.Contract;


public class Pair<A, B> {

    /**
     * The first value in the tuple
     */
    private final A a;

    /**
     * The second value in the tuple
     */
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

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
     * Gets the first value in the tuple, Synonym for first()
     */
    @Contract(pure = true)
    public A a() {
        return a;
    }

    /**
     * Gets the second value in the tuple, Synonym for second()
     */
    @Contract(pure = true)
    public B b() {
        return b;
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