/*
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
 */

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.info

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeHealthyInfo
import org.springframework.stereotype.Component

trait NodeHealthyInfoManager {

  def getNodeHealthyInfo(): NodeHealthyInfo

}


@Component
class DefaultNodeHealthyInfoManager extends NodeHealthyInfoManager with Logging {


  override def getNodeHealthyInfo(): NodeHealthyInfo = {
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg("")
    nodeHealthyInfo.setNodeHealthy(NodeStatus.isEngineNodeHealthy(ExecutorManager.getInstance.getReportExecutor.asInstanceOf[AccessibleExecutor].getStatus))
    nodeHealthyInfo
  }

}