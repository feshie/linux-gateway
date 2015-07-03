package org.mountainsensing.fetcher.utils;

/**
 * A simple immutable pair compromised of a Key and a Value.
 * @param <K> The type of the Key
 * @param <V> The type of the Value
 */
public class Pair<K,V> {
    
    /**
     * The key.
     */
    private final K key;
    
    /**
     * The value.
     */
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key of this pair.
     * @return The key of this pair.
     */
    public K getKey() {
        return this.key;
    }
    
    /**
     * Get the value of this pair.
     * @return The value of this pair.
     */
    public V getValue() {
        return this.value;
    }
}
