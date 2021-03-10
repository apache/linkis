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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process

import java.util

import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.{EngineConnCreationDesc, EngineConnLaunchRequest}
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait ProcessEngineConnLaunchRequest extends EngineConnLaunchRequest {

  val bmlResources: util.List[BmlResource]

  val environment: util.Map[String, String]

  val necessaryEnvironments: Array[String]

  val commands: Array[String]

  val maxRetries: Int

}

case class CommonProcessEngineConnLaunchRequest(ticketId: String,
                                                 user: String,
                                                 labels: util.List[Label[_]],
                                                 nodeResource: NodeResource,
                                                 bmlResources: util.List[BmlResource],
                                                 environment: util.Map[String, String],
                                                 necessaryEnvironments: Array[String],
                                                 creationDesc:  EngineConnCreationDesc,
                                                 engineConnManagerHooks: util.List[String],
                                                 commands: Array[String],
                                                 maxRetries: Int = 1
                                               ) extends ProcessEngineConnLaunchRequest