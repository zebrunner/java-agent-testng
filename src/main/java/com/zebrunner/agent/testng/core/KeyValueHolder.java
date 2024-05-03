package com.zebrunner.agent.testng.core;

import java.util.Map;
import java.util.Objects;

final class KeyValueHolder<K, V> implements Map.Entry<K, V> {
    final K key;
    final V value;

    KeyValueHolder(K k, V v) {
        key = Objects.requireNonNull(k);
        value = Objects.requireNonNull(v);
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Map.Entry<?, ?>
                && key.equals(((Map.Entry<?, ?>) o).getKey())
                && value.equals(((Map.Entry<?, ?>) o).getValue());
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
