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

package org.apache.linkis.rpc.sender

import org.apache.linkis.rpc.{Receiver, Sender}

import java.util

import scala.concurrent.duration.Duration

private class UnionSender private (receiver: Receiver, recycler: Receiver) extends Sender {

  override def ask(message: Any): Any =
    receiver.receiveAndReply(message, UnionSender.getUnionSender(recycler, receiver))

  override def ask(message: Any, timeout: Duration): Any =
    receiver.receiveAndReply(message, timeout, UnionSender.getUnionSender(recycler, receiver))

  override def send(message: Any): Unit =
    receiver.receive(message, UnionSender.getUnionSender(recycler, receiver))

  /**
   * Deliver is an asynchronous method that requests the target microservice asynchronously,
   * ensuring that the target microservice is requested once, but does not guarantee that the target
   * microservice will successfully receive the request.
   * deliver是一个异步方法，该方法异步请求目标微服务，确保一定会请求目标微服务一次，但不保证目标微服务一定能成功接收到本次请求。
   * @param message
   *   Requested parameters(请求的参数)
   */
  override def deliver(message: Any): Unit = send(message)
}

object UnionSender {
  private val receiverStringToSenders = new util.HashMap[String, Sender]

  def getUnionSender(receiver: Receiver, recycler: Receiver): Sender = {
    val key = receiver.hashCode() + "_" + recycler.hashCode()
    if (!receiverStringToSenders.containsKey(key)) receiverStringToSenders synchronized {
      if (!receiverStringToSenders.containsKey(key)) {
        receiverStringToSenders.put(key, new UnionSender(receiver, recycler))
      }
    }
    receiverStringToSenders.get(key)
  }

}
