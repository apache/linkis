/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.resourcemanager.utils

import org.apache.linkis.common.conf.{CommonVars, TimeType}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.serializer.NodeResourceSerializer
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.server.BDPJettyServerHelper
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConverters.asScalaBufferConverter

object RMUtils extends Logging {

  lazy val GSON = BDPJettyServerHelper.gson
  implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
  val mapper = BDPJettyServerHelper.jacksonJson

  val MANAGER_KILL_ENGINE_EAIT = CommonVars("wds.linkis.manager.rm.kill.engine.wait", new TimeType("30s"))

  val RM_REQUEST_ENABLE = CommonVars("wds.linkis.manager.rm.request.enable", true)

  val RM_RESOURCE_LOCK_WAIT_TIME = CommonVars("wds.linkis.manager.rm.lock.wait", 5 * 60 * 1000)

  val RM_DEBUG_ENABLE = CommonVars("wds.linkis.manager.rm.debug.enable", false)

  val RM_DEBUG_LOG_PATH = CommonVars("wds.linkis.manager.rm.debug.log.path", "file:///tmp/linkis/rmLog")

  val EXTERNAL_RESOURCE_REFRESH_TIME = CommonVars("wds.linkis.manager.rm.external.resource.regresh.time", new TimeType("30m"))

  val GOVERNANCE_STATION_ADMIN = CommonVars("wds.linkis.governance.station.admin", "hadoop")

  val COMBINED_USERCREATOR_ENGINETYPE = "combined_userCreator_engineType"

  val ENGINE_TYPE = CommonVars.apply("wds.linkis.configuration.engine.type", EngineType.getAllEngineTypes.asScala.mkString(","))

  val AM_SERVICE_NAME = "linkis-cg-linkismanager"



  def deserializeResource(plainResource: String): Resource = {
    read[Resource](plainResource)
  }

  def serializeResource(resource: Resource): String = {
    write(resource)
  }


  def toPersistenceResource(nodeResource: NodeResource) : PersistenceResource = {
    val persistenceResource = new PersistenceResource
    if(nodeResource.getMaxResource != null) persistenceResource.setMaxResource(serializeResource(nodeResource.getMaxResource))
    if(nodeResource.getMinResource != null) persistenceResource.setMinResource(serializeResource(nodeResource.getMinResource))
    if(nodeResource.getLockedResource != null) persistenceResource.setLockedResource(serializeResource(nodeResource.getLockedResource))
    if(nodeResource.getExpectedResource != null) persistenceResource.setExpectedResource(serializeResource(nodeResource.getExpectedResource))
    if(nodeResource.getLeftResource != null) persistenceResource.setLeftResource(serializeResource(nodeResource.getLeftResource))
    persistenceResource.setResourceType(nodeResource.getResourceType.toString())
    persistenceResource
  }



  def aggregateNodeResource(firstNodeResource: NodeResource, secondNodeResource: NodeResource) : CommonNodeResource = {
    if (firstNodeResource != null && secondNodeResource != null) {
      val aggregatedNodeResource = new CommonNodeResource
      aggregatedNodeResource.setResourceType(firstNodeResource.getResourceType)
      aggregatedNodeResource.setMaxResource(aggregateResource(firstNodeResource.getMaxResource, secondNodeResource.getMaxResource))
      aggregatedNodeResource.setMinResource(aggregateResource(firstNodeResource.getMinResource, secondNodeResource.getMinResource))
      aggregatedNodeResource.setUsedResource(aggregateResource(firstNodeResource.getUsedResource, secondNodeResource.getUsedResource))
      aggregatedNodeResource.setLockedResource(aggregateResource(firstNodeResource.getLockedResource, secondNodeResource.getLockedResource))
      aggregatedNodeResource.setLeftResource(aggregateResource(firstNodeResource.getLeftResource, secondNodeResource.getLeftResource))
      return aggregatedNodeResource
    }
    if (firstNodeResource == null && secondNodeResource == null) {
      return null
    }
    if(firstNodeResource == null) {
      return secondNodeResource.asInstanceOf[CommonNodeResource]
    } else {
      return firstNodeResource.asInstanceOf[CommonNodeResource]
    }
  }

  def aggregateResource(firstResource: Resource, secondResource: Resource): Resource = {
    (firstResource, secondResource) match {
      case (null, null) => null
      case (null, secondResource) => secondResource
      case (firstResource, null) => firstResource
      case (firstResource, secondResource) if firstResource.getClass.equals(secondResource.getClass) => firstResource.add(secondResource)
      case _ => null
    }
  }

}
