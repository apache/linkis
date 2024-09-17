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

package org.apache.linkis.engineplugin.server.service

import org.apache.linkis.manager.engineplugin.common.launch.process.{
  EngineConnResource,
  EngineConnResourceGenerator
}
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.protocol.message.{RequestMethod, RequestProtocol}

abstract class EngineConnResourceService extends EngineConnResourceGenerator {

  def init(): Unit

  def refreshAll(wait: Boolean, force: Boolean = false): Unit

  def refresh(engineConnRefreshRequest: RefreshEngineConnResourceRequest, force: Boolean): Boolean

  def getEngineConnBMLResources(
      engineConnBMLResourceRequest: GetEngineConnResourceRequest
  ): EngineConnResource

  override def getEngineConnBMLResources(engineTypeLabel: EngineTypeLabel): EngineConnResource = {
    val engineConnBMLResourceRequest = new GetEngineConnResourceRequest
    engineConnBMLResourceRequest.setEngineConnType(engineTypeLabel.getEngineType)
    engineConnBMLResourceRequest.setVersion(engineTypeLabel.getVersion)
    getEngineConnBMLResources(engineConnBMLResourceRequest)
  }

}

abstract class EngineConnResourceRequest extends RequestProtocol with RequestMethod {

  private var engineConnType: String = _
  private var version: String = _

  private var force: Boolean = false

  def getEngineConnType: String = engineConnType

  def setEngineConnType(engineConnType: String): Unit = this.engineConnType = engineConnType

  def getVersion: String = version

  def setVersion(version: String): Unit = this.version = version

  def getForce: Boolean = force

  def setForce(force: Boolean): Unit = this.force = force
}

class RefreshEngineConnResourceRequest extends EngineConnResourceRequest {
  override def method(): String = "/enginePlugin/engineConn/refresh"
}

class GetEngineConnResourceRequest extends EngineConnResourceRequest {
  override def method(): String = "/enginePlugin/engineConn/getResource"
}

class RefreshAllEngineConnResourceRequest extends RequestProtocol with RequestMethod {
  override def method(): String = "/enginePlugin/engineConn/refreshAll"
}
