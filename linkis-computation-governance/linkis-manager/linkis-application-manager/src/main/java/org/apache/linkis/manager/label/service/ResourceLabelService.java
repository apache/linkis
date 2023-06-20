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

package org.apache.linkis.manager.label.service;

import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.label.entity.Label;

public interface ResourceLabelService {

  void setEngineConnResourceToLabel(Label<?> label, NodeResource nodeResource, String source);

  /**
   * 通过传入的Labels 查找所有和Resource相关的Label 包含CombineLabel，需要CombineLabel的所有Label Key 出现过
   *
   * @param labels
   * @return
   */
  java.util.List<Label<?>> getResourceLabels(java.util.List<Label<?>> labels);

  /**
   * 设置某个Label的资源数值，如果不存在add，存在对应的Label update
   *
   * @param label
   * @param resource
   */
  void setResourceToLabel(Label<?> label, NodeResource resource, String source);

  /**
   * 通过Label 返回对应的Resource
   *
   * @param label
   * @return
   */
  NodeResource getResourceByLabel(Label<?> label);

  /**
   * 清理Label的资源信息和记录 1. 清理Label对应的Resource信息 2. 清理包含改Label的CombinedLabel的Resource信息
   *
   * @param label
   */
  void removeResourceByLabel(Label<?> label);

  void removeResourceByLabels(java.util.List<Label<?>> labels);

  PersistenceResource getPersistenceResourceByLabel(Label<?> label);
}
