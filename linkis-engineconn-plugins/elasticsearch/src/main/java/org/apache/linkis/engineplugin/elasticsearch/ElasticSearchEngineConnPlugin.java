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

package org.apache.linkis.engineplugin.elasticsearch;

import org.apache.linkis.engineplugin.elasticsearch.builder.ElasticSearchProcessEngineConnLaunchBuilder;
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration;
import org.apache.linkis.engineplugin.elasticsearch.factory.ElasticSearchEngineConnFactory;
import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin;
import org.apache.linkis.manager.engineplugin.common.creation.EngineConnFactory;
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder;
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceFactory;
import org.apache.linkis.manager.engineplugin.common.resource.GenericEngineResourceFactory;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineType;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticSearchEngineConnPlugin implements EngineConnPlugin {

  private EngineResourceFactory engineResourceFactory;

  private EngineConnFactory engineFactory;

  private List<Label<?>> defaultLabels = new ArrayList<>();

  private Object resourceLocker = new Object();

  private Object engineFactoryLocker = new Object();

  @Override
  public void init(Map<String, Object> params) {
    EngineTypeLabel typeLabel = new EngineTypeLabel();
    typeLabel.setEngineType(EngineType.ELASTICSEARCH().toString());
    typeLabel.setVersion(ElasticSearchConfiguration.DEFAULT_VERSION.getValue());
    this.defaultLabels.add(typeLabel);
  }

  @Override
  public EngineResourceFactory getEngineResourceFactory() {
    if (engineResourceFactory == null) {
      synchronized (resourceLocker) {
        if (engineResourceFactory == null) {
          engineResourceFactory = new GenericEngineResourceFactory();
        }
      }
    }
    return engineResourceFactory;
  }

  @Override
  public EngineConnLaunchBuilder getEngineConnLaunchBuilder() {
    return new ElasticSearchProcessEngineConnLaunchBuilder();
  }

  @Override
  public EngineConnFactory getEngineConnFactory() {
    if (engineFactory == null) {
      synchronized (engineFactoryLocker) {
        if (engineFactory == null) {
          engineFactory = new ElasticSearchEngineConnFactory();
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
