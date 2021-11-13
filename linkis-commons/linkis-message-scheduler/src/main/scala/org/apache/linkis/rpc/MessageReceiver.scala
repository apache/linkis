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
 
package org.apache.linkis.rpc

import java.util.concurrent.{TimeUnit, TimeoutException}

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.message.builder.{DefaultServiceMethodContext, MessageJobTimeoutPolicy, ServiceMethodContext}
import org.apache.linkis.message.conf.MessageSchedulerConf.{SENDER_KEY, _}
import org.apache.linkis.message.publisher.MessagePublisher
import org.apache.linkis.protocol.message.RequestProtocol
import org.apache.linkis.server.security.SecurityFilter
import javax.servlet.http.HttpServletRequest

import scala.concurrent.duration.Duration
import scala.language.implicitConversions


class MessageReceiver(mesagePublisher: MessagePublisher) extends Receiver {

  private val syncMaxTimeout: Duration = Duration(CommonVars("wds.linkis.ms.rpc.sync.timeout", 60 * 1000 * 5L).getValue, TimeUnit.MILLISECONDS)

  override def receive(message: Any, sender: Sender): Unit = {
    mesagePublisher.publish(message, (message, syncMaxTimeout, sender))
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = {
    receiveAndReply(message, syncMaxTimeout, sender)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {
    val job = mesagePublisher.publish(message, (message, duration, sender))
    Utils.tryCatch(job.get(duration._1, duration._2)) {
      case t: TimeoutException =>
        job.getMethodContext.getAttributeOrDefault(TIMEOUT_POLICY, MessageJobTimeoutPolicy.INTERRUPT) match {
          case MessageJobTimeoutPolicy.CANCEL => job.cancel(false); throw t
          case MessageJobTimeoutPolicy.INTERRUPT => job.cancel(true); throw t
          case MessageJobTimeoutPolicy.PARTIAL => job.getPartial
        }
      case i: InterruptedException => job.cancel(true); throw i
      case t: Throwable => job.cancel(true); throw t
    }
  }

  implicit def createMessageMethodScheduler(tunple: (Any, Duration, Sender)): ServiceMethodContext = {
    val methodContext = new DefaultServiceMethodContext
    methodContext.putAttribute(SENDER_KEY, tunple._3)
    methodContext.putAttribute(DURATION_KEY, tunple._2)
    tunple._1 match {
      case m: java.util.Map[String, Object] => {
        val req = m.get(REQUEST_KEY).asInstanceOf[HttpServletRequest]
        methodContext.putAttribute(REQUEST_KEY, req)
        methodContext.putAttribute(USER_KEY, SecurityFilter.getLoginUser(req))
      }
      case _ =>
    }
    methodContext
  }

  implicit def any2RequestProtocol(message: Any): RequestProtocol = message match {
    case p: RequestProtocol => p
    case m: java.util.Map[String, Object] => m.get("_request_protocol_").asInstanceOf[RequestProtocol]
  }


}
