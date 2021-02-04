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

package com.webank.wedatasphere.linkis.rpc.utils

import java.lang.reflect.UndeclaredThrowableException
import java.net.ConnectException

import com.netflix.client.ClientException
import com.webank.wedatasphere.linkis.rpc.exception.NoInstanceExistsException
import com.webank.wedatasphere.linkis.rpc.sender.SpringCloudFeignConfigurationCache
import feign.RetryableException
import org.apache.commons.lang.StringUtils
import scala.collection.JavaConversions._

/**
  * Created by enjoyyin on 2019/2/22.
  */
object RPCUtils {

  def isReceiverNotExists(t: Throwable): Boolean = t match {
    case connect: ConnectException => connect.getMessage != null && connect.getMessage.contains("Connection refused")
    case _: NoInstanceExistsException => true
    case t: UndeclaredThrowableException =>
      t.getCause match {
        case _: NoInstanceExistsException => true
        case _ => false
      }
    case t: RetryableException => t.getCause match {
      case connect: ConnectException =>
        connect.getMessage != null && connect.getMessage.contains("Connection refused")
      case _ => false
    }
    case t: RuntimeException => t.getCause match {
      case client: ClientException => StringUtils.isNotBlank(client.getErrorMessage) &&
        client.getErrorMessage.contains("Load balancer does not have available server for client")
      case _ => false
    }
    case _ => false
  }

  def findService(parsedServiceId: String, tooManyDeal: List[String] => Option[String]): Option[String] = {
    val services = SpringCloudFeignConfigurationCache.getDiscoveryClient
      .getServices.filter(_.toLowerCase.contains(parsedServiceId.toLowerCase)).toList
    if(services.length == 1) Some(services.head)
    else if(services.length > 1) tooManyDeal(services)
    else None
  }
}
