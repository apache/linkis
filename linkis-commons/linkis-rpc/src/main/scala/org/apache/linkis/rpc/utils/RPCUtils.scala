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

package org.apache.linkis.rpc.utils

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.rpc.{BaseRPCSender, Sender}
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.exception.NoInstanceExistsException
import org.apache.linkis.rpc.sender.{SpringCloudFeignConfigurationCache, SpringMVCRPCSender}

import org.apache.commons.lang3.StringUtils

import java.lang.reflect.UndeclaredThrowableException
import java.net.ConnectException
import java.util.Locale

import scala.collection.JavaConverters._

import com.netflix.client.ClientException
import feign.RetryableException

object RPCUtils {

  def isReceiverNotExists(t: Throwable): Boolean = t match {
    case connect: ConnectException =>
      connect.getMessage != null && connect.getMessage.contains("Connection refused")
    case _: NoInstanceExistsException => true
    case t: UndeclaredThrowableException =>
      t.getCause match {
        case _: NoInstanceExistsException => true
        case _ => false
      }
    case t: RetryableException =>
      t.getCause match {
        case connect: ConnectException =>
          connect.getMessage != null && connect.getMessage.contains("Connection refused")
        case _ => false
      }
    case t: RuntimeException =>
      t.getCause match {
        case client: ClientException =>
          StringUtils.isNotBlank(client.getErrorMessage) &&
            client.getErrorMessage.contains(
              "Load balancer does not have available server for client"
            )
        case _ => false
      }
    case _ => false
  }

  def findService(
      parsedServiceId: String,
      tooManyDeal: List[String] => Option[String]
  ): Option[String] = {
    val services = SpringCloudFeignConfigurationCache.getDiscoveryClient.getServices.asScala
      .filter(
        _.toLowerCase(Locale.getDefault)
          .contains(parsedServiceId.toLowerCase(Locale.getDefault()))
      )
      .toList
    if (services.length == 1) Some(services.head)
    else if (services.length > 1) tooManyDeal(services)
    else None
  }

  def getServiceInstanceFromSender(sender: Sender): ServiceInstance = {
    sender match {
      case springMVCRPCSender: SpringMVCRPCSender =>
        springMVCRPCSender.serviceInstance
      case baseRPCSender: BaseRPCSender =>
        ServiceInstance(baseRPCSender.getApplicationName, null)
      case _ =>
        null
    }
  }

  def isPublicService(appName: String): Boolean = {
    if (!RPCConfiguration.ENABLE_PUBLIC_SERVICE.getValue || StringUtils.isBlank(appName)) {
      return false
    }
    val appNameLower = appName.toLowerCase()
    if (appNameLower.startsWith(RPCConfiguration.PUBLIC_SERVICE_APP_PREFIX)) {
      val serviceName = appNameLower.replaceFirst(RPCConfiguration.PUBLIC_SERVICE_APP_PREFIX, "")
      RPCConfiguration.PUBLIC_SERVICE_LIST.exists(_.equalsIgnoreCase(serviceName))
    } else {
      false
    }
  }

}
