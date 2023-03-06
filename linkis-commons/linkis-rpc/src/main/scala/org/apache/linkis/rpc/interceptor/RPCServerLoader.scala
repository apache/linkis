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

package org.apache.linkis.rpc.interceptor

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.rpc.errorcode.LinkisRpcErrorCodeSummary.APPLICATION_IS_NOT_EXISTS
import org.apache.linkis.rpc.exception.NoInstanceExistsException
import org.apache.linkis.rpc.sender.SpringCloudFeignConfigurationCache

import java.text.MessageFormat

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

import com.netflix.loadbalancer.{DynamicServerListLoadBalancer, ILoadBalancer, Server}

trait RPCServerLoader {

  @throws[NoInstanceExistsException]
  def getOrRefreshServiceInstance(serviceInstance: ServiceInstance): Unit

  @throws[NoInstanceExistsException]
  def getServer(lb: ILoadBalancer, serviceInstance: ServiceInstance): Server

  def getServiceInstances(applicationName: String): Array[ServiceInstance]

}

abstract class AbstractRPCServerLoader extends RPCServerLoader with Logging {

  type SpringCloudServiceInstance = org.springframework.cloud.client.ServiceInstance

  val refreshMaxWaitTime: Duration

  def refreshAllServers(): Unit

  protected def refreshServerList(lb: ILoadBalancer): Unit = {
    refreshAllServers()
    lb match {
      case d: DynamicServerListLoadBalancer[_] => d.updateListOfServers()
      case _ =>
    }
  }

  private def getOrRefresh(
      refresh: => Unit,
      refreshed: => Boolean,
      serviceInstance: ServiceInstance
  ): Unit = {
    val instanceNotExists = new NoInstanceExistsException(
      APPLICATION_IS_NOT_EXISTS.getErrorCode,
      MessageFormat.format(
        APPLICATION_IS_NOT_EXISTS.getErrorDesc,
        serviceInstance.getInstance,
        serviceInstance.getApplicationName
      )
    )
    if (!refreshed) {
      Utils.tryThrow(
        Utils.waitUntil(
          () => {
            refresh
            val isRefreshed = refreshed
            if (!isRefreshed) {
              logger.info(
                s"Need a $serviceInstance, but cannot find in DiscoveryClient refresh list."
              )
            }
            isRefreshed
          },
          refreshMaxWaitTime,
          500,
          2000
        )
      ) { t =>
        instanceNotExists.initCause(t)
        instanceNotExists
      }
    }
  }

  override def getOrRefreshServiceInstance(serviceInstance: ServiceInstance): Unit = getOrRefresh(
    refreshAllServers(),
    getServiceInstances(serviceInstance.getApplicationName).contains(serviceInstance),
    serviceInstance
  )

  override def getServer(lb: ILoadBalancer, serviceInstance: ServiceInstance): Server = {
    getOrRefresh(
      refreshServerList(lb),
      lb.getAllServers.asScala.exists(_.getHostPort == serviceInstance.getInstance),
      serviceInstance
    )
    lb.getAllServers.asScala.find(_.getHostPort == serviceInstance.getInstance).get
  }

  def getDWCServiceInstance(serviceInstance: SpringCloudServiceInstance): ServiceInstance

  override def getServiceInstances(applicationName: String): Array[ServiceInstance] =
    SpringCloudFeignConfigurationCache.getDiscoveryClient
      .getInstances(applicationName)
      .iterator()
      .asScala
      .map { s =>
        val serviceInstance = getDWCServiceInstance(s)
        serviceInstance.setApplicationName(
          applicationName
        ) // 必须set，因为spring.application.name是区分大小写的，但是Discovery可能不区分
        serviceInstance
      }
      .toArray

}
