/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.manager.am.label

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.service.common.label.ManagerLabelService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._


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
    val isEngine = list.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
    if (!isEngine) {
      list.exists {
        case _: EMInstanceLabel =>
          true
        case _ => false
      }
    } else {
      false
    }
  }

  override def isEngine(labels: util.List[Label[_]]): Boolean = {
    labels.exists {
      case _: EngineInstanceLabel =>
        true
      case _ => false
    }
  }
}
