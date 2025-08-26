package de.jakob.lotm.util.data;

import java.util.Objects;

public class CompositeKey<K1, K2> {
    private final K1 key1;
    private final K2 key2;

    public CompositeKey(K1 key1, K2 key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    public K1 getKey1() {
        return key1;
    }

    public K2 getKey2() {
        return key2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CompositeKey<?, ?> that = (CompositeKey<?, ?>) obj;
        return Objects.equals(key1, that.key1) && Objects.equals(key2, that.key2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key1, key2);
    }

    @Override
    public String toString() {
        return String.format("CompositeKey{%s, %s}", key1, key2);
    }

    // Static factory methods for cleaner syntax
    public static <K1, K2> CompositeKey<K1, K2> of(K1 key1, K2 key2) {
        return new CompositeKey<>(key1, key2);
    }
}