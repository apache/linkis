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

package org.apache.linkis.manager.am.label

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.service.common.label.ManagerLabelService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

import scala.collection.JavaConverters._

@Service
class DefaultManagerLabelService extends ManagerLabelService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  override def isEngine(serviceInstance: ServiceInstance): Boolean = {
    val list = nodeLabelService.getNodeLabels(serviceInstance)
    isEngine(list)
  }

  override def isEM(serviceInstance: ServiceInstance): Boolean = {
    val list = nodeLabelService.getNodeLabels(serviceInstance)
    val isEngine = list.asScala.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
    if (!isEngine) {
      list.asScala.exists {
        case _: EMInstanceLabel =>
          true
        case _ => false
      }
    } else {
      false
    }
  }

  override def isEngine(labels: util.List[Label[_]]): Boolean = {
    labels.asScala.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
  }

}
