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

package org.apache.linkis.metadata.query.common.cache;

import com.google.common.cache.Cache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConnCacheManagerTest {

  @Test
  @DisplayName("customTest")
  public void customTest() {

    CacheManager cacheManager = ConnCacheManager.custom();
    Assertions.assertNotNull(cacheManager);
  }

  @Test
  @DisplayName("buildCacheTest")
  public void buildCacheTest() {

    CacheManager cacheManager = ConnCacheManager.custom();
    Cache<String, Object> cache =
        cacheManager.buildCache(
            "key",
            notification -> {
              assert notification.getValue() != null;
            });
    Assertions.assertNotNull(cache);
  }
}
