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
 
package org.apache.linkis.entranceclient

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.context.DefaultEntranceContext
import org.apache.linkis.entrance.exception.EntranceErrorException
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext
import org.apache.linkis.entrance.server.DefaultEntranceServer
import org.apache.linkis.entrance.{EntranceContext, EntranceServer}
import org.apache.linkis.entranceclient.execute._
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelScheduler
import org.apache.linkis.server.JMap
import org.apache.commons.lang.StringUtils

class EntranceClientImpl private() extends EntranceClient with EngineApplicationNameFactory
  with EngineManagerApplicationNameFactory with Logging {

  private var entranceServer: EntranceServer = _
  private var clientName: String = _


  def setClientName(clientName: String): Unit = this.clientName = clientName


  def init(entranceServer: EntranceServer): Unit = {
    this.entranceServer = entranceServer
  }

  def init(entranceContext: EntranceContext): Unit = init(new DefaultEntranceServer(entranceContext))

  def init(clientEntranceParser: context.ClientEntranceParser, schedulerContext: EntranceSchedulerContext, interceptors: Array[EntranceInterceptor],
           maxParallelismUsers: Int): Unit = if (entranceServer == null) synchronized {
    if (entranceServer == null) {
      val scheduler = new ParallelScheduler(schedulerContext)
      scheduler.init()
      scheduler.start()
      val entranceContext = new DefaultEntranceContext(clientEntranceParser, new context.ClientPersistenceManager, new context.ClientLogManager,
        scheduler, interceptors, null, Array.empty)
      init(entranceContext)
    }
  }

  private def executeUntil[T](code: String, user: String, creator: String,
                              params: java.util.Map[String, Any], op: ClientJob => T): T = {
    val execId = executeJob(code, user, creator, params)
    entranceServer.getJob(execId).foreach {
      case job: ClientJob =>
        job.waitForComplete()
        return op(job)
    }
    throw new EntranceErrorException(50003, s"execute failed, cannot find the job $execId.")
  }

  override def getEntranceClientName: String = if(StringUtils.isNotEmpty(clientName)) clientName else entranceServer.getName

  override def execute(code: String, user: String, creator: String): Boolean = execute(code, user, creator, null)

  override def execute(code: String, user: String, creator: String,
                       params: java.util.Map[String, Any]): Boolean =
    executeUntil(code, user, creator, params, job =>
      if (job.isSucceed) true else false)

  override def executeJob(code: String, user: String, creator: String): String = executeJob(code, user, creator, null)

  override def executeJob(code: String, user: String, creator: String, params: util.Map[String, Any]): String = {
    val requestMap = new JMap[String, Any]
    requestMap.put(TaskConstant.EXECUTIONCODE, code)
    requestMap.put(TaskConstant.UMUSER, user)
    requestMap.put(TaskConstant.REQUESTAPPLICATIONNAME, creator)
    if(params != null && !params.isEmpty) {
      if(params.containsKey(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)) {
        requestMap.put(EntranceServer.DO_NOT_PRINT_PARAMS_LOG, params.get(EntranceServer.DO_NOT_PRINT_PARAMS_LOG))
        params.remove(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)
      }
      if(!params.isEmpty) requestMap.put(TaskConstant.PARAMS, params)
    }
    entranceServer.execute(requestMap)
  }

  override def getJob(jobId: String): Option[ClientJob] = entranceServer.getJob(jobId).map { case job: ClientJob => job }

  override def executeResult(code: String, user: String, creator: String): Array[String] =
    executeResult(code, user, creator, null)

  override def executeResult(code: String, user: String, creator: String,
                             params: java.util.Map[String, Any]): Array[String] =
    executeUntil(code, user, creator, params, job =>
      if(job.isSucceed) job.getResultSets
      else if(job.getErrorResponse != null) {
        val exception = new EntranceErrorException(22001, job.getErrorResponse.message)
        if(job.getErrorResponse.t != null) exception.initCause(job.getErrorResponse.t)
        throw exception
      } else throw new EntranceErrorException(22002, "execute failed, unknown reason."))

}
object EntranceClientImpl {

  private val clientNameToEntranceClient = new JMap[String, EntranceClientImpl]

  def apply(clientName: String): EntranceClientImpl = {
    if(!clientNameToEntranceClient.containsKey(clientName)) synchronized {
      if(!clientNameToEntranceClient.containsKey(clientName)) {
        val client = new EntranceClientImpl
        client.setClientName(clientName)
        clientNameToEntranceClient.put(clientName, client)
      }
    }
    clientNameToEntranceClient.get(clientName)
  }

  def getClientNames = clientNameToEntranceClient.keySet()
}