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
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.rpc.interceptor.{RPCInterceptor, RPCServerLoader}
import org.apache.linkis.rpc.interceptor.common.BroadcastSenderBuilder

import java.util

import scala.collection.JavaConverters._

private[rpc] object RPCSpringBeanCache extends Logging {
  import DataWorkCloudApplication.getApplicationContext
  private var beanNameToReceivers: util.Map[String, Receiver] = _
  private var rpcInterceptors: Array[RPCInterceptor] = _
  private var rpcServerLoader: RPCServerLoader = _
  private var senderBuilders: Array[BroadcastSenderBuilder] = _
  private var rpcReceiveRestful: RPCReceiveRestful = _

  def registerReceiver(receiverName: String, receiver: Receiver): Unit = {
    if (beanNameToReceivers == null) {
      beanNameToReceivers = getApplicationContext.getBeansOfType(classOf[Receiver])
    }
    logger.info(s"register a new receiver with name $receiverName, receiver is " + receiver)
    beanNameToReceivers synchronized beanNameToReceivers.put(receiverName, receiver)
  }

  def registerReceiverChooser(receiverChooser: ReceiverChooser): Unit = {
    if (rpcReceiveRestful == null) {
      rpcReceiveRestful = getApplicationContext.getBean(classOf[RPCReceiveRestful])
    }
    rpcReceiveRestful.registerReceiverChooser(receiverChooser)
  }

  def registerBroadcastListener(broadcastListener: BroadcastListener): Unit = {
    if (rpcReceiveRestful == null) {
      rpcReceiveRestful = getApplicationContext.getBean(classOf[RPCReceiveRestful])
    }
    rpcReceiveRestful.registerBroadcastListener(broadcastListener)
  }

  def getRPCReceiveRestful: RPCReceiveRestful = {
    if (rpcReceiveRestful == null) {
      rpcReceiveRestful = getApplicationContext.getBean(classOf[RPCReceiveRestful])
    }
    rpcReceiveRestful
  }

  private[rpc] def getReceivers: util.Map[String, Receiver] = {
    if (beanNameToReceivers == null) {
      beanNameToReceivers = getApplicationContext.getBeansOfType(classOf[Receiver])
    }
    beanNameToReceivers
  }

  private[rpc] def getRPCInterceptors: Array[RPCInterceptor] = {
    if (rpcInterceptors == null) {
      rpcInterceptors = getApplicationContext
        .getBeansOfType(classOf[RPCInterceptor])
        .asScala
        .map(_._2)
        .toArray
        .sortBy(_.order)
    }
    rpcInterceptors
  }

  private[rpc] def getRPCServerLoader: RPCServerLoader = {
    if (rpcServerLoader == null) {
      rpcServerLoader = getApplicationContext.getBean(classOf[RPCServerLoader])
    }
    rpcServerLoader
  }

  private[rpc] def getBroadcastSenderBuilders: Array[BroadcastSenderBuilder] = {
    if (senderBuilders == null) {
      senderBuilders = getApplicationContext
        .getBeansOfType(classOf[BroadcastSenderBuilder])
        .asScala
        .map(_._2)
        .toArray
        .sortBy(_.order)
    }
    senderBuilders
  }

}
