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

package org.apache.linkis.engineconnplugin.sqoop.executor

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.once.executor.{
  OnceExecutorExecutionContext,
  OperableOnceExecutor
}
import org.apache.linkis.engineconnplugin.sqoop.client.LinkisSqoopClient
import org.apache.linkis.engineconnplugin.sqoop.client.errorcode.SqoopErrorCodeSummary.EXEC_SQOOP_CODE_ERROR
import org.apache.linkis.engineconnplugin.sqoop.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.sqoop.context.{
  SqoopEngineConnContext,
  SqoopEnvConfiguration
}
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopResourceConfiguration.{
  LINKIS_QUEUE_NAME,
  LINKIS_SQOOP_TASK_MAP_CPU_CORES,
  LINKIS_SQOOP_TASK_MAP_MEMORY
}
import org.apache.linkis.engineconnplugin.sqoop.params.SqoopParamsResolver
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse

import java.util
import java.util.Locale
import java.util.concurrent.{Future, TimeUnit}

class SqoopOnceCodeExecutor(
    override val id: Long,
    override protected val sqoopEngineConnContext: SqoopEngineConnContext
) extends SqoopOnceExecutor
    with OperableOnceExecutor {

  private var params: util.Map[String, String] = _
  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  private val paramsResolvers: Array[SqoopParamsResolver] = Array()

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = {
    var isFailed = false
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        // TODO filter job content
        params = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent
          .asInstanceOf[util.Map[String, String]]
        logger.info("Try to execute params." + params)
        if (runSqoop(params, onceExecutorExecutionContext.getEngineCreationContext) != 0) {
          isFailed = true
          tryFailed()
          setResponse(
            ErrorExecuteResponse(
              "Run code failed!",
              new JobExecutionException(EXEC_SQOOP_CODE_ERROR.getErrorDesc)
            )
          )
        }
        logger.info("All codes completed, now to stop SqoopEngineConn.")
        closeDaemon()
        if (!isFailed) {
          trySucceed()
        }
        this synchronized notify()
      }
    })
  }

  protected def runSqoop(params: util.Map[String, String], context: EngineCreationContext): Int = {
    Utils.tryCatch {
      val finalParams = paramsResolvers.foldLeft(params) { case (newParam, resolver) =>
        resolver.resolve(newParam, context)
      }
      LinkisSqoopClient.run(finalParams)
    } { case e: Exception =>
      logger.error(s"Run Error Message: ${e.getMessage}", e)
      -1
    }

  }

  override protected def waitToRunning(): Unit = {
    if (!isCompleted) {
      daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(
        new Runnable {
          override def run(): Unit = {
            if (!(future.isDone || future.isCancelled)) {
              logger.info("The Sqoop Process In Running")
            }
          }
        },
        SqoopEnvConfiguration.SQOOP_STATUS_FETCH_INTERVAL.getValue.toLong,
        SqoopEnvConfiguration.SQOOP_STATUS_FETCH_INTERVAL.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
    }
  }

  override def getCurrentNodeResource(): NodeResource = {
    val memorySuffix = "g"
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    Option(properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)).foreach(memory => {
      if (!memory.toLowerCase(Locale.getDefault()).endsWith(memorySuffix)) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, memory + memorySuffix)
      }
    })
    val resource = new DriverAndYarnResource(
      new LoadInstanceResource(
        EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
        EngineConnPluginConf.JAVA_ENGINE_REQUEST_CORES.getValue(properties),
        EngineConnPluginConf.JAVA_ENGINE_REQUEST_INSTANCE
      ),
      new YarnResource(
        LINKIS_SQOOP_TASK_MAP_MEMORY.getValue * getNumTasks,
        LINKIS_SQOOP_TASK_MAP_CPU_CORES.getValue * getNumTasks,
        0,
        LINKIS_QUEUE_NAME.getValue
      )
    )
    val engineResource = new CommonNodeResource
    engineResource.setUsedResource(resource)
    engineResource
  }

  def getNumTasks: Int = {
    if (params != null) {
      params.getOrDefault("sqoop.args.num.mappers", "1").toInt
    } else {
      0
    }
  }

  protected def closeDaemon(): Unit = {
    if (daemonThread != null) daemonThread.cancel(true)
  }

  override def getProgress: Float = LinkisSqoopClient.progress()

  override def getProgressInfo: Array[JobProgressInfo] = {
    val progressInfo = LinkisSqoopClient.getProgressInfo
    logger.info(
      s"Progress Info, id: ${progressInfo.id}, total: ${progressInfo.totalTasks}, running: ${progressInfo.runningTasks}," +
        s" succeed: ${progressInfo.succeedTasks}, fail: ${progressInfo.failedTasks}"
    )
    Array(progressInfo)
  }

  override def getMetrics: util.Map[String, Any] = {
    val metrics = LinkisSqoopClient.getMetrics.asInstanceOf[util.Map[String, Any]]
    // Report the resource
    metrics.put("NodeResourceJson", getCurrentNodeResource().getUsedResource.toJson)
    metrics
  }

  override def getDiagnosis: util.Map[String, Any] =
    LinkisSqoopClient.getDiagnosis.asInstanceOf[util.Map[String, Any]]

}
