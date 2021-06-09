/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.resourcemanager.service

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceResource
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer

abstract class LabelResourceService {

  def getLabelResource(label: Label[_]): NodeResource

  def setLabelResource(label: Label[_], nodeResource: NodeResource) : Unit

  /**
   * 方法同 setLabelResource 只适用于启动引擎申请资源后设置engineConn资源
   * this function is the same to setLabelResource
   * @param label
   * @param nodeResource
   */
  def setEngineConnLabelResource(label: Label[_], nodeResource: NodeResource) : Unit

  def getResourcesByUser(user: String) : Array[NodeResource]

  // 需要通过AM传入的Label（用户的Label和EMLabel）从LabelResource拿到所有关系的Label：里面包含所有的单Label和CombineLabel。
  def enrichLabels(labelContainer: RMLabelContainer) : RMLabelContainer

  def removeResourceByLabel(label: Label[_]): Unit

  def getLabelsByResource(resource: PersistenceResource): Array[Label[_]]

}