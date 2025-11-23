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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.manager.persistence.*;

public class DefaultManagerPersistence implements ManagerPersistence {

  private NodeManagerPersistence nodeManagerPersistence;
  private LabelManagerPersistence labelManagerPersistence;
  private LockManagerPersistence lockManagerPersistence;
  private ResourceManagerPersistence resourceManagerPersistence;
  private NodeMetricManagerPersistence nodeMetricManagerPersistence;
  private ResourceLabelPersistence resourceLabelPersistence;

  @Override
  public NodeMetricManagerPersistence getNodeMetricManagerPersistence() {
    return nodeMetricManagerPersistence;
  }

  public void setNodeMetricManagerPersistence(
      NodeMetricManagerPersistence nodeMetricManagerPersistence) {
    this.nodeMetricManagerPersistence = nodeMetricManagerPersistence;
  }

  @Override
  public ResourceManagerPersistence getResourceManagerPersistence() {
    return resourceManagerPersistence;
  }

  public void setResourceManagerPersistence(ResourceManagerPersistence resourceManagerPersistence) {
    this.resourceManagerPersistence = resourceManagerPersistence;
  }

  @Override
  public NodeManagerPersistence getNodeManagerPersistence() {
    return nodeManagerPersistence;
  }

  public void setNodeManagerPersistence(NodeManagerPersistence nodeManagerPersistence) {
    this.nodeManagerPersistence = nodeManagerPersistence;
  }

  @Override
  public LabelManagerPersistence getLabelManagerPersistence() {
    return labelManagerPersistence;
  }

  public void setLabelManagerPersistence(LabelManagerPersistence labelManagerPersistence) {
    this.labelManagerPersistence = labelManagerPersistence;
  }

  @Override
  public LockManagerPersistence getLockManagerPersistence() {
    return lockManagerPersistence;
  }

  public void setLockManagerPersistence(LockManagerPersistence lockManagerPersistence) {
    this.lockManagerPersistence = lockManagerPersistence;
  }

  public ResourceLabelPersistence getResourceLabelPersistence() {
    return resourceLabelPersistence;
  }

  public void setResourceLabelPersistence(ResourceLabelPersistence resourceLabelPersistence) {
    this.resourceLabelPersistence = resourceLabelPersistence;
  }
}
