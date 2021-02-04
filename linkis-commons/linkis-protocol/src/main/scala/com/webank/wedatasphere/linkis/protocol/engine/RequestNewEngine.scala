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

import java.util

import com.webank.wedatasphere.linkis.protocol.RetryableProtocol

/**
  * Created by enjoyyin on 2018/9/18.
  */
trait RequestEngine extends RetryableProtocol {
  val creator: String
  val user: String
  val properties: util.Map[String, String]  //Other parameter information(其他参数信息)
}
object RequestEngine {
  private val header = "_req_"
  val REQUEST_ENTRANCE_INSTANCE = header + "entrance_instance"
//  val ENGINE_MAX_FREE_TIME = header + "engine_max_free_time"
//  val ENGINE_MAX_EXECUTE_NUM = header + "engine_max_execute_num"
  //  val ENGINE_USER = header + "engine_user"
  val ENGINE_INIT_SPECIAL_CODE = header + "engine_init_code"
  def isRequestEngineProperties(key: String) = key.startsWith(header)
}
trait TimeoutRequestEngine {
  val timeout: Long
}
case class RequestNewEngine(creator: String, user: String, properties: util.Map[String, String]) extends RequestEngine
case class TimeoutRequestNewEngine(timeout: Long, user: String, creator: String, properties: util.Map[String, String]) extends RequestEngine with TimeoutRequestEngine
case class RequestKillEngine(instance: String, killApplicationName: String, killInstance: String)