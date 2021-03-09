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

package com.webank.wedatasphere.linkis.entranceclient

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.context.DefaultEntranceContext
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.execute.impl.ConcurrentEngineSelector
import com.webank.wedatasphere.linkis.entrance.execute.{EngineSelector, EntranceExecutorManager, EntranceExecutorRuler, EntranceReceiver}
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceSchedulerContext
import com.webank.wedatasphere.linkis.entrance.server.DefaultEntranceServer
import com.webank.wedatasphere.linkis.entrance.{EntranceContext, EntranceServer}
import com.webank.wedatasphere.linkis.entranceclient.execute._
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.rpc.ClientReceiverChooser
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.{ParallelConsumerManager, ParallelScheduler}
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils

/**
  * Created by johnnwang on 2018/10/30.
  */
class EntranceClientImpl private() extends EntranceClient with EngineApplicationNameFactory
  with EngineManagerApplicationNameFactory with Logging {

  private var entranceServer: EntranceServer = _
  private var clientName: String = _
  private var receiverChooser: Option[ClientReceiverChooser] = _

  def setClientName(clientName: String): Unit = this.clientName = clientName

  private def dealEngineApplicationNameFactory(dealEngineApplicationNameFactory: Array[EngineApplicationNameFactory] => Unit): Unit =
    if(entranceServer != null)
      entranceServer.getEntranceContext.getOrCreateScheduler().getSchedulerContext match {
        case clientSchedulerContext: EntranceSchedulerContext =>
          clientSchedulerContext.getOrCreateExecutorManager match {
            case clientExecutorManager: EntranceExecutorManager =>
              dealEngineApplicationNameFactory(Array(clientExecutorManager.getOrCreateEngineBuilder() match {
                case engineBuilder: ClientEngineBuilder => engineBuilder
                case _ => throw new exception.ClientErrorException(25000, "The injected EngineBuilder must inherit from ClientEngineBuilder.(注入的EngineBuilder必须继承ClientEngineBuilder.)")
              },
              clientExecutorManager.getOrCreateEngineManager() match {
                case engineManager: ClientEngineManager => engineManager
                case _ => throw new exception.ClientErrorException(25000, "The injected EngineManager must inherit from ClientEngineManager.(注入的EngineManager必须继承ClientEngineManager.)")
              }))
          }
      }

  private def dealEngineManagerApplicationNameFactory(dealEngineManagerApplicationNameFactory: Array[EngineManagerApplicationNameFactory] => Unit): Unit =
    if(entranceServer != null)
      entranceServer.getEntranceContext.getOrCreateScheduler().getSchedulerContext match {
        case clientSchedulerContext: EntranceSchedulerContext =>
          clientSchedulerContext.getOrCreateExecutorManager match {
            case clientExecutorManager: EntranceExecutorManager =>
              dealEngineManagerApplicationNameFactory(Array(clientExecutorManager.getOrCreateEngineRequester() match {
                case engineRequest: ClientEngineRequester => engineRequest
                case _ => throw new exception.ClientErrorException(25000, "The injected EngineRequester must inherit ClientEngineRequester.(注入的EngineRequester必须继承ClientEngineRequester.)")
              }))
          }
      }

  override def setEngineApplicationName(engineApplicationName: String): Unit = {
    super.setEngineApplicationName(engineApplicationName)
    dealEngineApplicationNameFactory(_.foreach(_.setEngineApplicationName(engineApplicationName)))
    if(receiverChooser != null) receiverChooser.foreach(_.setEngineApplicationName(engineApplicationName))
  }


  override def setEngineManagerApplicationName(engineManagerApplicationName: String): Unit = {
    super.setEngineManagerApplicationName(engineManagerApplicationName)
    dealEngineManagerApplicationNameFactory(_.foreach(_.setEngineManagerApplicationName(engineManagerApplicationName)))
    if(receiverChooser != null)   receiverChooser.foreach(_.setEngineManagerApplicationName(engineManagerApplicationName))
  }

  def getReceiverChooser = receiverChooser

  def init(entranceServer: EntranceServer): Unit = {
    this.entranceServer = entranceServer
    receiverChooser = Some(new ClientReceiverChooser)
    receiverChooser.foreach(_.setReceiver(new EntranceReceiver(entranceServer.getEntranceContext)))
    dealEngineApplicationNameFactory(_.foreach(_.getEngineApplicationName)) //Used only to determine if a class is legal(仅仅用于判断类是否合法)
    dealEngineManagerApplicationNameFactory(_.foreach(_.getEngineManagerApplicationName)) //Used only to determine if a class is legal(仅仅用于判断类是否合法)
    if (!isEngineApplicationNameEmpty) setEngineApplicationName(getEngineApplicationName)
    if (!isEngineManagerApplicationNameEmpty) setEngineManagerApplicationName(getEngineManagerApplicationName)
  }

  def init(entranceContext: EntranceContext): Unit = init(new DefaultEntranceServer(entranceContext))

  def init(clientEntranceParser: context.ClientEntranceParser, groupFactory: scheduler.ClientGroupFactory, engineBuilder: ClientEngineBuilder,
           engineRequester: ClientEngineRequester, engineSelector: EngineSelector, interceptors: Array[EntranceInterceptor],
           entranceExecutorRulers: Array[EntranceExecutorRuler],
           maxParallelismUsers: Int): Unit = if(entranceServer == null) synchronized {
    if (entranceServer == null) {
      val schedulerContext = new EntranceSchedulerContext(groupFactory, new ParallelConsumerManager(maxParallelismUsers),
        new ClientEntranceExecutorManager(groupFactory, engineRequester, engineBuilder, engineSelector, new ClientEngineManager, entranceExecutorRulers))
      val scheduler = new ParallelScheduler(schedulerContext)
      val entranceContext = new DefaultEntranceContext(clientEntranceParser, new context.ClientPersistenceManager, new context.ClientLogManager,
        scheduler, interceptors, null, Array.empty)
      init(entranceContext)
    }
  }

  def init(groupFactory: scheduler.ClientGroupFactory, engineBuilder: ClientEngineBuilder,
           engineRequester: ClientEngineRequester, entranceExecutorRulers: Array[EntranceExecutorRuler], maxParallelismUsers: Int): Unit =
    init(new context.ClientEntranceParser, groupFactory, engineBuilder, engineRequester,
      new ConcurrentEngineSelector, Array.empty, entranceExecutorRulers, maxParallelismUsers)

  def init(maxParallelismUsers: Int): Unit = {
    val groupFactory = new scheduler.ClientGroupFactory
    init(groupFactory, new ClientEngineBuilder(groupFactory), new ClientEngineRequester, Array.empty, maxParallelismUsers)
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
      if(job.isSucceed) true else false)

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

  override def getJob(jobId: String): Option[ClientJob] = entranceServer.getJob(jobId).map{case job: ClientJob => job}

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