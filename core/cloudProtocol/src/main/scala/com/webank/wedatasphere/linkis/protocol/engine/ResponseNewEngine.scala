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

package com.webank.wedatasphere.linkis.protocol.engine

import com.webank.wedatasphere.linkis.protocol.{BroadcastProtocol, RetryableProtocol}

/**
  * Created by enjoyyin on 2018/9/18.
  */
trait ResponseEngine extends RetryableProtocol {
  val port: Int
  val status: Int
  val initErrorMsg: String
}

/**
  * engine send to engineManager
  * @param pid
  */
case class ResponseEnginePid(port: Int, pid: String) extends RetryableProtocol
/**
  * engine send to engineManager
  * @param port
  * @param status
  * @param initErrorMsg
  */
case class ResponseEngineStatusCallback(override val port: Int,
                                override val status: Int,
                                override val initErrorMsg: String) extends ResponseEngine

/**
  * engineManager send to entrance
  * @param instance
  * @param responseEngineStatus
  */
  case class ResponseNewEngineStatus(instance: String, responseEngineStatus: ResponseEngineStatusCallback) extends RetryableProtocol

/**
  * engineManager send to entrance
  * @param applicationName
  * @param instance
  */
case class ResponseNewEngine(applicationName: String, instance: String) extends RetryableProtocol

case class BroadcastNewEngine(responseNewEngine: ResponseNewEngine, responseEngineStatus: ResponseEngineStatus)
  extends BroadcastProtocol