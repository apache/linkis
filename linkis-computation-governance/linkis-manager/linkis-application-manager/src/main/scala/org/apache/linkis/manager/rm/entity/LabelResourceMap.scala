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

package org.apache.linkis.manager.rm.entity

import org.apache.linkis.manager.common.entity.resource.Resource
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.rm.entity.ResourceOperationType.ResourceOperationType

class LabelResourceMapping(
    label: Label[_],
    resource: Resource,
    resourceOperationType: ResourceOperationType,
    user: String
) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case labelResourceMap: LabelResourceMapping =>
        labelResourceMap.getLabel().getStringValue.equals(label.getStringValue)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    super.hashCode()
  }

  def getLabel(): Label[_] = label

  def getResource(): Resource = resource

  def getResourceOperationType: ResourceOperationType = resourceOperationType

  def getUser: String = user

  override def toString: String = {
    s"Label ${label.getStringValue} mapping resource ${resource}"
  }

}
