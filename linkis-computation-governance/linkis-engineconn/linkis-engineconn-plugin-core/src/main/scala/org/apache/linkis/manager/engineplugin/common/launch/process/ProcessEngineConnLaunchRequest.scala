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

package org.apache.linkis.manager.engineplugin.common.launch.process

import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.launch.entity.{
  EngineConnCreationDesc,
  EngineConnLaunchRequest
}
import org.apache.linkis.manager.label.entity.Label

import java.util

trait ProcessEngineConnLaunchRequest extends EngineConnLaunchRequest {

  val bmlResources: util.List[BmlResource]

  val environment: util.Map[String, String]

  val necessaryEnvironments: Array[String]

  val commands: Array[String]

  val maxRetries: Int

}

case class CommonProcessEngineConnLaunchRequest(
    ticketId: String,
    user: String,
    labels: util.List[Label[_]],
    nodeResource: NodeResource,
    bmlResources: util.List[BmlResource],
    environment: util.Map[String, String],
    necessaryEnvironments: Array[String],
    creationDesc: EngineConnCreationDesc,
    engineConnManagerHooks: util.List[String],
    commands: Array[String],
    maxRetries: Int = 1
) extends ProcessEngineConnLaunchRequest
