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

package org.apache.linkis.engineconn.computation.executor.upstream.access

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconn.computation.executor.upstream.entity.ECTaskEntranceConnection
import org.apache.linkis.engineconn.computation.executor.upstream.wrapper.ECTaskEntranceConnectionWrapper
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationErrorCode
import org.apache.linkis.rpc.sender.SpringCloudFeignConfigurationCache

import org.springframework.cloud.client.{ServiceInstance => SpringCloudServiceInstance}

import java.util
import java.util.Locale

import scala.collection.JavaConverters._

/**
 * check entrance by DiscoveryClient
 */
class ECTaskEntranceInfoAccess extends ConnectionInfoAccess with Logging {
  val discoveryClient = SpringCloudFeignConfigurationCache.getDiscoveryClient

  // queryUpstreamInfo
  override def getUpstreamInfo(
      request: ConnectionInfoAccessRequest
  ): List[ECTaskEntranceConnection] = {
    panicIfNull(request, "ConnectionInfoAccessRequest should not be null")
    panicIfNull(discoveryClient, "discoveryClient should not be null")

    val ret: util.List[ECTaskEntranceConnection] = new util.ArrayList[ECTaskEntranceConnection]

    request match {
      case eCTaskEntranceInfoAccessRequest: ECTaskEntranceInfoAccessRequest =>
        // val instances = Sender.getInstances(GovernanceCommonConf.ENTRANCE_SPRING_NAME.getValue) //use discoveryClient
        val instanceMap = new util.HashMap[String, ServiceInstance]
        Utils.tryCatch(discoveryClient.getServices.asScala.map(s => {
          discoveryClient.getInstances(s).asScala.map { s1 =>
            {
              val s3 = getDWCServiceInstance(s1)
              instanceMap.put(s3.getInstance, s3) // instance should be unique
            }
          }
        })) { t =>
          throw EngineConnException(
            ComputationErrorCode.UPSTREAM_MONITOR_EXCEPTION,
            "Failed to get services from Service Registry"
          ).initCause(t)
        }
        if (instanceMap.size() == 0) {
          throw EngineConnException(
            ComputationErrorCode.UPSTREAM_MONITOR_EXCEPTION,
            "Got none serviceInstances from Service Registry"
          )
        }

        val currentTime = System.currentTimeMillis
        val wrappers = eCTaskEntranceInfoAccessRequest.getData
        panicIfNull(wrappers, "wrappers should not be null")
        val elements = wrappers.iterator
        while (elements.hasNext) {
          val wrapper = elements.next
          if (wrapper == null) {
            logger.warn("wrapper should not be null")
          } else {
            wrapper match {
              case ecWrapper: ECTaskEntranceConnectionWrapper =>
                val engineConnTask = ecWrapper.getEngineConnTask
                val instance = engineConnTask.getCallbackServiceInstance
                val eCTaskEntranceConnection =
                  new ECTaskEntranceConnection(engineConnTask.getTaskId, "", instance.getInstance)
                if (isConnectionAlive(instance, instanceMap)) {
                  eCTaskEntranceConnection.updatePrevAliveTimeStamp(currentTime)
                }
                ret.add(eCTaskEntranceConnection)
              case _ =>
                logger.warn(
                  "invalid data-type: " + wrapper.getClass.getCanonicalName + " for data in ECTaskEntranceInfoAccessRequest"
                )
            }
          }
        }
      case _ =>
        throw EngineConnException(
          ComputationErrorCode.INVALID_DATA_TYPE_ERROR_CODE,
          "invalid data-type: " + request.getClass.getCanonicalName
        )
    }
    ret.iterator().asScala.toList
  }

  private def getDWCServiceInstance(
      serviceInstance: SpringCloudServiceInstance
  ): ServiceInstance = {
    val applicationName = serviceInstance.getServiceId
    ServiceInstance(
      applicationName.toLowerCase(Locale.getDefault),
      s"${serviceInstance.getHost}:${serviceInstance.getPort}"
    )
  }

  private def isConnectionAlive(
      instance: ServiceInstance,
      instanceMap: util.HashMap[String, ServiceInstance]
  ): Boolean = {
    instanceMap
      .containsKey(instance.getInstance) && instanceMap.get(instance.getInstance).equals(instance)
  }

  protected def panicIfNull(obj: Any, msg: String): Unit = {
    if (obj == null) {
      throw EngineConnException(ComputationErrorCode.VARIABLE_NULL_ERROR_CODE, msg)
    }
  }

}
