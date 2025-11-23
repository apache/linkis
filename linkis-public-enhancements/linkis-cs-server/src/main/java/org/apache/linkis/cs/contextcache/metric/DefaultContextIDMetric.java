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

public class DefaultContextIDMetric implements ContextIDMetric {

  private int usedCount;

  private long memory;

  private long cachedTime = System.currentTimeMillis();

  private long accessTime = System.currentTimeMillis();

  @Override
  public int getUsedCount() {
    return this.usedCount;
  }

  @Override
  public void addCount() {
    this.usedCount++;
  }

  @Override
  public long getMemory() {
    return this.memory;
  }

  @Override
  public void setMemory(long memory) {
    if (memory < 0) {
      memory = 0;
    }
    this.memory = memory;
  }

  @Override
  public long getCachedTime() {
    return this.cachedTime;
  }

  @Override
  public long getLastAccessTime() {
    return this.accessTime;
  }

  @Override
  public void setLastAccessTime(long accessTime) {
    this.accessTime = accessTime;
  }
}
