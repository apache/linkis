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

package org.apache.linkis.manager.rm.service;

import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;

import java.util.List;

public abstract class LabelResourceService {

  public abstract NodeResource getLabelResource(Label<?> label);

  public abstract void setLabelResource(Label<?> label, NodeResource nodeResource, String source);

  /**
   * this function is the same to setLabelResource
   *
   * @param label
   * @param nodeResource
   */
  public abstract void setEngineConnLabelResource(
      Label<?> label, NodeResource nodeResource, String source);

  public abstract NodeResource[] getResourcesByUser(String user);

  public abstract RMLabelContainer enrichLabels(List<Label<?>> labels);

  public abstract void removeResourceByLabel(Label<?> label);

  public abstract Label<?>[] getLabelsByResource(PersistenceResource resource);

  public abstract PersistenceResource getPersistenceResource(Label<?> label);
}
