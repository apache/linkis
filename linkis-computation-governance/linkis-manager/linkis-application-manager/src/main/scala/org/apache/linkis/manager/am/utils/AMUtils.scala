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

package org.apache.linkis.manager.am.utils

import org.apache.linkis.manager.am.vo.{AMEngineNodeVo, EMNodeVo}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{EMNode, EngineNode}
import org.apache.linkis.manager.common.entity.resource.{
  DriverAndYarnResource,
  Resource,
  ResourceSerializer,
  ResourceType
}
import org.apache.linkis.manager.common.serializer.NodeResourceSerializer
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.server.BDPJettyServerHelper

import java.util

import scala.collection.JavaConverters._

import com.google.gson.JsonObject
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

object AMUtils {

  lazy val GSON = BDPJettyServerHelper.gson

  implicit val formats = DefaultFormats + ResourceSerializer + NodeResourceSerializer
  val mapper = BDPJettyServerHelper.jacksonJson

  def copyToEMVo(EMNodes: Array[EMNode]): util.ArrayList[EMNodeVo] = {
    val EMNodeVos = new util.ArrayList[EMNodeVo]()
    EMNodes.foreach(node => {
      val EMNodeVo = new EMNodeVo
      EMNodeVo.setLabels(node.getLabels)
      EMNodeVo.setApplicationName(node.getServiceInstance.getApplicationName)
      EMNodeVo.setInstance(node.getServiceInstance.getInstance)
      if (node.getStartTime != null) EMNodeVo.setStartTime(node.getStartTime)
      if (node.getNodeResource != null) {
        if (node.getNodeResource.getResourceType != null) {
          EMNodeVo.setResourceType(node.getNodeResource.getResourceType)
        }
        if (node.getNodeResource.getMaxResource != null) {
          EMNodeVo.setMaxResource(
            mapper
              .readValue(write(node.getNodeResource.getMaxResource), classOf[util.Map[String, Any]])
          )
        }
        if (node.getNodeResource.getMinResource != null) {
          EMNodeVo.setMinResource(
            mapper
              .readValue(write(node.getNodeResource.getMinResource), classOf[util.Map[String, Any]])
          )
        }
        if (node.getNodeResource.getUsedResource != null) {
          EMNodeVo.setUsedResource(
            mapper.readValue(
              write(node.getNodeResource.getUsedResource),
              classOf[util.Map[String, Any]]
            )
          )
        } else {
          EMNodeVo.setUsedResource(
            mapper.readValue(
              write(Resource.initResource(ResourceType.Default)),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getLockedResource != null) {
          EMNodeVo.setLockedResource(
            mapper.readValue(
              write(node.getNodeResource.getLockedResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getExpectedResource != null) {
          EMNodeVo.setExpectedResource(
            mapper.readValue(
              write(node.getNodeResource.getExpectedResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getLeftResource != null) {
          EMNodeVo.setLeftResource(
            mapper.readValue(
              write(node.getNodeResource.getLeftResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
      }
      EMNodeVo.setOwner(node.getOwner)
      if (node.getNodeTaskInfo != null) {
        if (node.getNodeTaskInfo.getRunningTasks >= 0) {
          EMNodeVo.setRunningTasks(node.getNodeTaskInfo.getRunningTasks)
        }
        if (node.getNodeTaskInfo.getPendingTasks >= 0) {
          EMNodeVo.setPendingTasks(node.getNodeTaskInfo.getPendingTasks)
        }
        if (node.getNodeTaskInfo.getSucceedTasks >= 0) {
          EMNodeVo.setSucceedTasks(node.getNodeTaskInfo.getSucceedTasks)
        }
        if (node.getNodeTaskInfo.getFailedTasks >= 0) {
          EMNodeVo.setFailedTasks(node.getNodeTaskInfo.getFailedTasks)
        }
      }
      if (node.getNodeOverLoadInfo != null) {
        if (node.getNodeOverLoadInfo.getMaxMemory != null) {
          EMNodeVo.setMaxMemory(node.getNodeOverLoadInfo.getMaxMemory)
        }
        if (node.getNodeOverLoadInfo.getUsedMemory != null) {
          EMNodeVo.setUsedMemory(node.getNodeOverLoadInfo.getUsedMemory)
        }
        if (node.getNodeOverLoadInfo.getSystemCPUUsed != null) {
          EMNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
        }
        if (node.getNodeOverLoadInfo.getSystemLeftMemory != null) {
          EMNodeVo.setSystemLeftMemory(node.getNodeOverLoadInfo.getSystemLeftMemory)
        }
      }
      if (node.getNodeHealthyInfo != null) {
        if (node.getNodeHealthyInfo.getNodeHealthy != null) {
          EMNodeVo.setNodeHealthy(node.getNodeHealthyInfo.getNodeHealthy)
        }
        if (node.getNodeHealthyInfo.getMsg != null) {
          EMNodeVo.setMsg(node.getNodeHealthyInfo.getMsg)
        }
      }
      EMNodeVos.add(EMNodeVo)
    })
    EMNodeVos
  }

  def copyToAMEngineNodeVo(
      AMEngineNode: java.util.List[EngineNode]
  ): java.util.ArrayList[AMEngineNodeVo] = {
    val AMEngineNodeVos = new util.ArrayList[AMEngineNodeVo]()
    if (!AMEngineNode.isEmpty) {
      AMEngineNode.asScala.foreach(node => {
        val AMEngineNodeVo = new AMEngineNodeVo
        AMEngineNodeVo.setLabels(node.getLabels)
        AMEngineNodeVo.setApplicationName(node.getServiceInstance.getApplicationName)
        AMEngineNodeVo.setInstance(node.getServiceInstance.getInstance)
        if (null != node.getEMNode) {
          AMEngineNodeVo.setEmInstance(node.getEMNode.getServiceInstance.getInstance)
        }

        if (!node.getLabels.isEmpty) {
          val engineTypeLabel =
            node.getLabels.asScala.find(_.isInstanceOf[EngineTypeLabel]).getOrElse(null)
          if (engineTypeLabel != null) {
            AMEngineNodeVo.setEngineType(
              engineTypeLabel.asInstanceOf[EngineTypeLabel] getEngineType
            )
          }
        }
        if (node.getStartTime != null) AMEngineNodeVo.setStartTime(node.getStartTime)
        if (node.getNodeStatus != null) {
          AMEngineNodeVo.setNodeStatus(node.getNodeStatus)
        } else {
          AMEngineNodeVo.setNodeStatus(NodeStatus.Starting)
        }
        if (node.getLock != null) AMEngineNodeVo.setLock(node.getLock)
        if (node.getNodeResource != null) {
          if (node.getNodeResource.getResourceType != null) {
            AMEngineNodeVo.setResourceType(node.getNodeResource.getResourceType)
          }
          if (node.getNodeResource.getMaxResource != null) {
            AMEngineNodeVo.setMaxResource(createUnlimitedResource)
          }
          if (node.getNodeResource.getMinResource != null) {
            AMEngineNodeVo.setMinResource(createZeroResource)
          }
          if (node.getNodeResource.getUsedResource != null) {
            val realResource = node.getNodeResource.getUsedResource match {
              case dy: DriverAndYarnResource => dy.loadInstanceResource
              case _ => node.getNodeResource.getUsedResource
            }
            AMEngineNodeVo.setUsedResource(
              mapper.readValue(write(realResource), classOf[util.Map[String, Any]])
            )
          } else {
            AMEngineNodeVo.setUsedResource(
              mapper.readValue(
                write(Resource.initResource(ResourceType.Default)),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getLockedResource != null) {
            AMEngineNodeVo.setLockedResource(
              mapper.readValue(
                write(node.getNodeResource.getLockedResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getExpectedResource != null) {
            AMEngineNodeVo.setExpectedResource(
              mapper.readValue(
                write(node.getNodeResource.getExpectedResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getLeftResource != null) {
            AMEngineNodeVo.setLeftResource(
              mapper.readValue(
                write(node.getNodeResource.getLeftResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
        }
        AMEngineNodeVo.setOwner(node.getOwner)
        if (node.getNodeTaskInfo != null) {
          if (node.getNodeTaskInfo.getRunningTasks >= 0) {
            AMEngineNodeVo.setRunningTasks(node.getNodeTaskInfo.getRunningTasks)
          }
          if (node.getNodeTaskInfo.getPendingTasks >= 0) {
            AMEngineNodeVo.setPendingTasks(node.getNodeTaskInfo.getPendingTasks)
          }
          if (node.getNodeTaskInfo.getSucceedTasks >= 0) {
            AMEngineNodeVo.setSucceedTasks(node.getNodeTaskInfo.getSucceedTasks)
          }
          if (node.getNodeTaskInfo.getFailedTasks >= 0) {
            AMEngineNodeVo.setFailedTasks(node.getNodeTaskInfo.getFailedTasks)
          }
        }
        if (node.getNodeOverLoadInfo != null) {
          if (node.getNodeOverLoadInfo.getMaxMemory != null) {
            AMEngineNodeVo.setMaxMemory(node.getNodeOverLoadInfo.getMaxMemory)
          }
          if (node.getNodeOverLoadInfo.getUsedMemory != null) {
            AMEngineNodeVo.setUsedMemory(node.getNodeOverLoadInfo.getUsedMemory)
          }
          if (node.getNodeOverLoadInfo.getSystemCPUUsed != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
          }
          if (node.getNodeOverLoadInfo.getSystemLeftMemory != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
          }
        }
        if (node.getNodeHealthyInfo != null) {
          if (node.getNodeHealthyInfo.getNodeHealthy != null) {
            AMEngineNodeVo.setNodeHealthy(node.getNodeHealthyInfo.getNodeHealthy)
          }
          if (node.getNodeHealthyInfo.getMsg != null) {
            AMEngineNodeVo.setMsg(node.getNodeHealthyInfo.getMsg)
          }
        }
        AMEngineNodeVos.add(AMEngineNodeVo)
      })
    }
    AMEngineNodeVos
  }

  def createUnlimitedResource(): util.Map[String, Long] = {
    val map = new util.HashMap[String, Long]()
    map.put("core", 128)
    map.put("memory", 512 * 1024 * 1024 * 1024)
    map.put("instance", 512)
    map
  }

  def createZeroResource(): util.Map[String, Long] = {
    val map = new util.HashMap[String, Long]()
    map.put("core", 1)
    map.put("memory", 512 * 1024 * 1024)
    map.put("instance", 0)
    map
  }

  def isJson(str: String): Boolean = {
    try {
      GSON.fromJson(str, classOf[JsonObject])
      true
    } catch {
      case _ => false
    }
  }

}
