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

package org.apache.linkis.manager.rm.entity;

import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.label.entity.Label;

import java.util.Objects;

public class LabelResourceMapping {

  private Label<?> label;
  private Resource resource;
  private ResourceOperationType resourceOperationType;

  public LabelResourceMapping(
      Label<?> label, Resource resource, ResourceOperationType resourceOperationType) {
    this.label = label;
    this.resource = resource;
    this.resourceOperationType = resourceOperationType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    LabelResourceMapping that = (LabelResourceMapping) obj;
    return Objects.equals(label.getStringValue(), that.label.getStringValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(label.getStringValue());
  }

  public Label<?> getLabel() {
    return label;
  }

  public Resource getResource() {
    return resource;
  }

  public ResourceOperationType getResourceOperationType() {
    return resourceOperationType;
  }

  @Override
  public String toString() {
    return String.format(
        "Label %s mapping resource %s", label.getStringValue(), resource.toString());
  }
}
