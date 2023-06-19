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

package org.apache.linkis.engineplugin.trino;

import org.apache.linkis.engineplugin.trino.builder.TrinoProcessEngineConnLaunchBuilder;
import org.apache.linkis.engineplugin.trino.factory.TrinoEngineConnFactory;
import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin;
import org.apache.linkis.manager.engineplugin.common.creation.EngineConnFactory;
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder;
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceFactory;
import org.apache.linkis.manager.engineplugin.common.resource.GenericEngineResourceFactory;
import org.apache.linkis.manager.label.entity.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrinoEngineConnPlugin implements EngineConnPlugin {

  private Object resourceLocker = new Object();
  private Object engineFactoryLocker = new Object();
  private volatile EngineResourceFactory engineResourceFactory;
  private volatile EngineConnFactory engineFactory;
  private final List<Label<?>> defaultLabels = new ArrayList<>();

  @Override
  public void init(Map<String, Object> params) {}

  @Override
  public EngineResourceFactory getEngineResourceFactory() {
    if (Objects.isNull(engineResourceFactory)) {
      synchronized (resourceLocker) {
        if (Objects.isNull(engineResourceFactory)) {
          engineResourceFactory = new GenericEngineResourceFactory();
        }
      }
    }
    return engineResourceFactory;
  }

  @Override
  public EngineConnLaunchBuilder getEngineConnLaunchBuilder() {
    return new TrinoProcessEngineConnLaunchBuilder();
  }

  @Override
  public EngineConnFactory getEngineConnFactory() {
    if (Objects.isNull(engineFactory)) {
      synchronized (engineFactoryLocker) {
        if (Objects.isNull(engineFactory)) {
          engineFactory = new TrinoEngineConnFactory();
        }
      }
    }
    return engineFactory;
  }

  @Override
  public List<Label<?>> getDefaultLabels() {
    return defaultLabels;
  }
}
