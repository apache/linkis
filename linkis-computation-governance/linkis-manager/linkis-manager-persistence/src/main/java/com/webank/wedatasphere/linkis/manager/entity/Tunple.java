package com.webank.wedatasphere.linkis.manager.entity;


public class Tunple<K, V> {
    private final K key;
    private final V value;

    public Tunple(K k, V v) {
        this.key = k;
        this.value = v;
    }

    public Tunple(Tunple<? extends K, ? extends V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }
}
