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

package com.webank.wedatasphere.linkis.manager.label.score

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.node.ScoreServiceInstance


class LabelScoreServiceInstance(instance: ServiceInstance) extends ScoreServiceInstance{
  var score = 0d
  var serviceInstance: ServiceInstance = instance
  override def getScore: Double = score

  override def setScore(score: Double): Unit = {
    this.score = score
  }

  override def getServiceInstance: ServiceInstance = serviceInstance

  override def setServiceInstance(serviceInstance: ServiceInstance): Unit = {

  }
}
