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

package org.apache.linkis.engineplugin.cache.refresh;

import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class RefreshPluginCacheOperation implements Delayed {
  private AtomicLong triggerTime = new AtomicLong(0);

  private TimeUnit timeUnit = TimeUnit.SECONDS;

  private long duration = 0L;

  private EngineConnPluginInfo pluginInfo;

  private Function<EngineConnPluginInfo, EngineConnPluginInstance> operation;

  public RefreshPluginCacheOperation(
      Function<EngineConnPluginInfo, EngineConnPluginInstance> operation) {
    this.operation = operation;
    this.triggerTime.set(System.currentTimeMillis());
  }

  void setDuration(long duration) {
    this.duration = duration;
  }

  void setTimeUnit(TimeUnit timeUnit) {
    if (null != timeUnit) {
      this.timeUnit = timeUnit;
    }
  }

  String cacheStringKey() {
    return this.pluginInfo != null ? this.pluginInfo.toString() : null;
  }

  EngineConnPluginInfo pluginInfo() {
    return this.pluginInfo;
  }

  void setPluginInfo(EngineConnPluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
  }

  public Function<EngineConnPluginInfo, EngineConnPluginInstance> getOperation() {
    return operation;
  }

  public void nextTime() {
    this.triggerTime.addAndGet(TimeUnit.MILLISECONDS.convert(duration, timeUnit));
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(this.triggerTime.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    if (o instanceof RefreshPluginCacheOperation) {
      return (int) (this.triggerTime.get() - ((RefreshPluginCacheOperation) o).triggerTime.get());
    }
    return -1;
  }
}
