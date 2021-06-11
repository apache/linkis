package com.webank.wedatasphere.linkis.protocol.util;

import java.util.AbstractMap;


public class ImmutablePair<K, V> {

    private AbstractMap.SimpleImmutableEntry<K, V> entry;

    public ImmutablePair(K k, V v) {
        entry = new AbstractMap.SimpleImmutableEntry<K, V>(k, v);
    }

    public K getKey() {
        if (null != entry) {
            return entry.getKey();
        } else {
            return null;
        }
    }

    public V getValue() {
        if (null != entry) {
            return entry.getValue();
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (getClass().isInstance(o)) {
            ImmutablePair<K, V> other = (ImmutablePair<K, V>) o;
            return eq(getKey(), other.getKey()) && eq(getValue(), other.getValue());
        } else {
            return false;
        }
    }

    private boolean eq(Object o1, Object o2) {
        if (null != o1 && null != o2) {
            return o1.equals(o2);
        } else if (o1 == o2){
            return true;
        } else {
            return false;
        }
    }
}

