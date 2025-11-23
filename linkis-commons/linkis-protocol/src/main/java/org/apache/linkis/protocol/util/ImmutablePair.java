/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.protocol.util;

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
    } else if (o1 == o2) {
      return true;
    } else {
      return false;
    }
  }
}
