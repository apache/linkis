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

package org.apache.linkis.rpc

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.rpc.utils.RPCUtils

import java.util

import scala.concurrent.duration.Duration

abstract class Sender {

  /**
   * Ask is a synchronous method that requests the target microservice in real time and requires the
   * target microservice to return a non-null return value.
   * ask是一个同步方法，该方法实时请求目标微服务，且要求目标微服务必须返回一个非空的返回值
   * @param message
   *   Requested parameter(请求的参数)
   * @return
   *   the response message
   */
  def ask(message: Any): Any

  /**
   * Same as [[ask(message: Any)]], just one more timeout <br> Please note: this timeout period
   * refers to the maximum processing time for the target microservice to process this request.
   * 同[[ask(message: Any)]]，只是多了一个超时时间<br> 请注意：该超时时间，是指目标微服务处理本次请求的最大处理时间
   * @param message
   *   Requested parameter(请求的参数)
   * @param timeout
   *   Maximum processing time(最大处理时间)
   * @return
   *   the response message
   */
  def ask(message: Any, timeout: Duration): Any

  /**
   * Send is a synchronization method that requests the target microservice in real time and ensures
   * that the target microservice successfully receives the request, but does not require the target
   * microservice to respond in real time.
   * send是一个同步方法，该方法实时请求目标微服务，并确保目标微服务成功接收了本次请求，但不要求目标微服务实时给出答复。
   * @param message
   *   Requested parameter(请求的参数)
   */
  def send(message: Any): Unit

  /**
   * Deliver is an asynchronous method that requests the target microservice asynchronously,
   * ensuring that the target microservice is requested once, but does not guarantee that the target
   * microservice will successfully receive the request.
   * deliver是一个异步方法，该方法异步请求目标微服务，确保一定会请求目标微服务一次，但不保证目标微服务一定能成功接收到本次请求。
   * @param message
   *   Requested parameter(请求的参数)
   */
  def deliver(message: Any): Unit
}

object Sender {
  // TODO needs to consider whether the sender will be a singleton, will there be communication problems?
  // TODO 需要考虑将sender做成单例后，会不会出现通信问题

  private val senderFactory: SenderFactory = SenderFactory.getFactory

  private val serviceInstanceToSenders = new util.HashMap[ServiceInstance, Sender]

  def getSender(applicationName: String): Sender = getSender(ServiceInstance(applicationName, null))

  def getSender(serviceInstance: ServiceInstance): Sender = {
    if (RPCUtils.isPublicService(serviceInstance.getApplicationName)) {
      serviceInstance.setApplicationName(RPCConfiguration.PUBLIC_SERVICE_APPLICATION_NAME.getValue)
    }
    if (!serviceInstanceToSenders.containsKey(serviceInstance)) {
      serviceInstanceToSenders synchronized {
        if (!serviceInstanceToSenders.containsKey(serviceInstance)) {
          serviceInstanceToSenders.put(serviceInstance, senderFactory.createSender(serviceInstance))
        }
      }
    }
    serviceInstanceToSenders.get(serviceInstance)
  }

  def getThisServiceInstance: ServiceInstance = DataWorkCloudApplication.getServiceInstance
  def getThisInstance: String = DataWorkCloudApplication.getInstance()

  def getInstances(applicationName: String): Array[ServiceInstance] =
    RPCSpringBeanCache.getRPCServerLoader.getServiceInstances(applicationName)

}
