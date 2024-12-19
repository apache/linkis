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

package org.apache.linkis.manager.am.label;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.service.common.label.ManagerLabelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultManagerLabelService implements ManagerLabelService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultManagerLabelService.class);

  @Autowired private NodeLabelService nodeLabelService;

  @Override
  public boolean isEngine(ServiceInstance serviceInstance) {
    List<Label<?>> labelList = nodeLabelService.getNodeLabels(serviceInstance);
    return isEngine(labelList);
  }

  @Override
  public boolean isEM(ServiceInstance serviceInstance) {
    List<Label<?>> labelList = nodeLabelService.getNodeLabels(serviceInstance);
    boolean isEngine = labelList.stream().anyMatch(label -> label instanceof EngineInstanceLabel);
    if (!isEngine) {
      return labelList.stream().anyMatch(label -> label instanceof EMInstanceLabel);
    } else {
      return false;
    }
  }

  @Override
  public boolean isEngine(List<Label<?>> labels) {
    return labels.stream().anyMatch(label -> label instanceof EngineInstanceLabel);
  }
}
