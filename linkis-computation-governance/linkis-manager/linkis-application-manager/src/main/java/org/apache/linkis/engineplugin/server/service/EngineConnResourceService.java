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

package org.apache.linkis.engineplugin.server.service;

import org.apache.linkis.manager.engineplugin.common.launch.process.EngineConnResource;
import org.apache.linkis.manager.engineplugin.common.launch.process.EngineConnResourceGenerator;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

public abstract class EngineConnResourceService implements EngineConnResourceGenerator {

  public abstract void init();

  public abstract void refreshAll(boolean wait, boolean force);

  public abstract boolean refresh(
      RefreshEngineConnResourceRequest engineConnRefreshRequest, boolean force);

  public abstract EngineConnResource getEngineConnBMLResources(
      GetEngineConnResourceRequest engineConnBMLResourceRequest);

  @Override
  public EngineConnResource getEngineConnBMLResources(EngineTypeLabel engineTypeLabel) {
    GetEngineConnResourceRequest engineConnBMLResourceRequest = new GetEngineConnResourceRequest();
    engineConnBMLResourceRequest.setEngineConnType(engineTypeLabel.getEngineType());
    engineConnBMLResourceRequest.setVersion(engineTypeLabel.getVersion());
    return getEngineConnBMLResources(engineConnBMLResourceRequest);
  }
}
