/*
 *
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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity

import java.util

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


trait EngineConnBuildRequest extends RequestProtocol {
  val ticketId: String
  val labels: util.List[Label[_]]
  val engineResource: NodeResource
  val engineConnCreationDesc: EngineConnCreationDesc
}


case class EngineConnBuildRequestImpl(ticketId: String,
                                      labels: util.List[Label[_]],
                                      engineResource: NodeResource,
                                      engineConnCreationDesc: EngineConnCreationDesc) extends EngineConnBuildRequest


trait RicherEngineConnBuildRequest extends EngineConnBuildRequest {

  def getBmlResources: Array[BmlResource]

  def getStartupConfigs: util.Map[String, Object]

}






