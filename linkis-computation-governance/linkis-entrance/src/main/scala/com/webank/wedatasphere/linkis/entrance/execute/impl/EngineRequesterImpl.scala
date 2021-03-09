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

package com.webank.wedatasphere.linkis.entrance.execute.impl

import java.util

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.ENGINE_CREATE_MAX_WAIT_TIME
import com.webank.wedatasphere.linkis.entrance.execute.{EngineRequester, EntranceJob}
import com.webank.wedatasphere.linkis.protocol.config.{RequestQueryGlobalConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, TimeoutRequestNewEngine}
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.server.JMap

import scala.collection.JavaConversions._

/**
  * Created by enjoyyin on 2018/9/26.
  */
class EngineRequesterImpl extends EngineRequester {
  override protected def createRequestEngine(job: Job): RequestEngine = job match {
      //TODO The maximum timeout period created should be read from the database(TODO 创建的最大超时时间，应该从数据库读取)
    case entranceJob: EntranceJob =>
      val sender = Sender.getSender(EntranceConfiguration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
      val requestQueryGlobalConfig = RequestQueryGlobalConfig(entranceJob.getUser)
      val responseQueryGlobalConfig = sender.ask(requestQueryGlobalConfig).asInstanceOf[ResponseQueryConfig]
      val keyAndValue = responseQueryGlobalConfig.getKeyAndValue
      val createTimeWait = keyAndValue.get(ENGINE_CREATE_MAX_WAIT_TIME.key)
      var createTimeWaitL = ENGINE_CREATE_MAX_WAIT_TIME.getValue.toLong
      if (createTimeWait != null ) createTimeWaitL = java.lang.Long.parseLong(createTimeWait)
      val properties = if(entranceJob.getParams == null) new util.HashMap[String, String]
      else {
        val startupMap = TaskUtils.getStartupMap(entranceJob.getParams)
        val properties = new JMap[String, String]
        startupMap.foreach {case (k, v) => if(v != null) properties.put(k, v.toString)}
        properties
      }
      properties.put(RequestEngine.REQUEST_ENTRANCE_INSTANCE, Sender.getThisServiceInstance.getApplicationName + "," + Sender.getThisServiceInstance.getInstance)
      TimeoutRequestNewEngine(createTimeWaitL, entranceJob.getUser, entranceJob.getCreator, properties)
    case _ => null
  }
}
