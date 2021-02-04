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

import com.webank.wedatasphere.linkis.protocol.RetryableProtocol

/**
  *
  * Request status information from an engine(向某一个engine请求状态信息)
  * Created by enjoyyin on 2018/9/27.
  */
case class RequestEngineStatus(messageType: Int) extends RetryableProtocol
object RequestEngineStatus {
  val Status_Only = 1
  val Status_Overload = 2
  val Status_Concurrent = 3
  val Status_Overload_Concurrent = 4
  val Status_BasicInfo = 5
  val ALL = 6
}