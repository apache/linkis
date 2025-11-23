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

package org.apache.linkis.cs.contextcache.metric;

public class DefaultContextCacheMetric implements ContextCacheMetric {

  private int usedCount;

  private int cachedCount;

  private long memory;

  @Override
  public int getUsedCount() {
    return this.usedCount;
  }

  @Override
  public void addCount() {
    this.usedCount++;
  }

  @Override
  public int getCachedCount() {
    return this.cachedCount;
  }

  @Override
  public void setCachedCount(int count) {
    if (count < 0) {
      count = 0;
    }
    this.cachedCount = count;
  }

  @Override
  public long getCachedMemory() {
    return this.memory;
  }

  @Override
  public void setCachedMemory(long memory) {
    if (memory < 0) {
      memory = 0;
    }
    this.memory = memory;
  }
}
