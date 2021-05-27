package com.webank.wedatasphere.linkis.manager.entity;

/**
 * created by v_wbjftang on 2020/8/25
 */
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
