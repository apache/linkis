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
package com.webank.wedatasphere.linkis.entrance.executer

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.exception.JDBCParamsIllegalException
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl
import com.webank.wedatasphere.linkis.scheduler.executer.Executor
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}
import org.apache.commons.lang.StringUtils

import scala.concurrent.duration.Duration


class JDBCEngineExecutorManagerImpl(groupFactory: GroupFactory,
                                    engineBuilder: EngineBuilder,
                                    engineRequester: EngineRequester,
                                    engineSelector: EngineSelector,
                                    engineManager: EngineManager,
                                    entranceExecutorRulers: Array[EntranceExecutorRuler])
  extends EntranceExecutorManagerImpl(groupFactory,engineBuilder, engineRequester,
    engineSelector, engineManager, entranceExecutorRulers) with Logging{
  private val JDBCEngineExecutor = new util.HashMap[String, JDBCEngineExecutor]()
  logger.info("JDBC EngineManager Registered")
  override protected def createExecutor(event: SchedulerEvent): EntranceEngine = event match {
    case job: JDBCEntranceJob =>
      val JDBCParams = new util.HashMap[String, String]()
      val params = job.getParams
      val url = if (params.get("jdbc.url") != null) params.get("jdbc.url").toString
      else throw JDBCParamsIllegalException("jdbc url is null")
      val username = if (params.get("jdbc.username") != null) params.get("jdbc.username").toString
      else throw JDBCParamsIllegalException("jdbc username is null")
      val password = if (params.get("jdbc.password") != null) params.get("jdbc.password").toString
      else throw JDBCParamsIllegalException("jdbc password is null")
      JDBCParams.put("jdbc.url",url)
      JDBCParams.put("jdbc.username",username)
      JDBCParams.put("jdbc.password",password)
      if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
        JDBCEngineExecutor.put(url + ":" + username + ":" + password, new JDBCEngineExecutor(5000, JDBCParams))
         new JDBCEngineExecutor(5000, JDBCParams)
      }else {
        logger.error(s"jdbc url is $url, jdbc username is $username")
        throw JDBCParamsIllegalException("jdbc url or username or password may be null at least")
      }
  }

  override def setExecutorListener(executorListener: ExecutorListener): Unit = ???

  override def askExecutor(event: SchedulerEvent): Option[Executor] = event match{
    case job:JDBCEntranceJob =>
      findUsefulExecutor(job).orElse(Some(createExecutor(event)))
    case _ => None
  }


  override def askExecutor(event: SchedulerEvent, wait: Duration): Option[Executor] = event match {
    case job:JDBCEntranceJob =>
      findUsefulExecutor(job).orElse(Some(createExecutor(event)))
    case _ => None
  }


  private def findUsefulExecutor(job: Job): Option[Executor] = job match{
    case job:JDBCEntranceJob =>
      val params = job.getParams
      val url = if (params.get("jdbc.url") != null) params.get("jdbc.url").toString
      else throw JDBCParamsIllegalException("jdbc url is null")
      val username = if (params.get("jdbc.username") != null) params.get("jdbc.username").toString
      else throw JDBCParamsIllegalException("jdbc username is null")
      val password = if (params.get("jdbc.password") != null) params.get("jdbc.password").toString
      else throw JDBCParamsIllegalException("jdbc password is null")
      val key = url + ":" + username + ":" + password
      if (JDBCEngineExecutor.containsKey(key)){
        Some(JDBCEngineExecutor.get(key))
      }else{
        None
      }
  }



  override def getById(id: Long): Option[Executor] = ???

  override def getByGroup(groupName: String): Array[Executor] = ???

  override protected def delete(executor: Executor): Unit = ???

  override def shutdown(): Unit = ???

}
