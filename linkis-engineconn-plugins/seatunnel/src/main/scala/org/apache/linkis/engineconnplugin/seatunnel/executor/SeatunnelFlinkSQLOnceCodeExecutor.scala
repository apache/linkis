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

package org.apache.linkis.engineconnplugin.seatunnel.executor

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.conf.EngineConnConf.ENGINE_CONN_LOCAL_PATH_PWD_KEY
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationEngineUtils.GSON
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.once.executor.{
  OnceExecutorExecutionContext,
  OperableOnceExecutor
}
import org.apache.linkis.engineconnplugin.seatunnel.client.LinkisSeatunnelFlinkSQLClient
import org.apache.linkis.engineconnplugin.seatunnel.client.errorcode.SeatunnelErrorCodeSummary.EXEC_FLINKSQL_CODE_ERROR
import org.apache.linkis.engineconnplugin.seatunnel.client.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelEnvConfiguration
import org.apache.linkis.engineconnplugin.seatunnel.config.SeatunnelFlinkEnvConfiguration.{
  GET_LINKIS_FLINK_CHECK,
  GET_LINKIS_FLINK_CONFIG,
  GET_LINKIS_FLINK_RUNMODE,
  GET_LINKIS_FLINK_VARIABLE,
  LINKIS_FLINK_CHECK,
  LINKIS_FLINK_CONFIG,
  LINKIS_FLINK_RUNMODE,
  LINKIS_FLINK_VARIABLE
}
import org.apache.linkis.engineconnplugin.seatunnel.context.SeatunnelEngineConnContext
import org.apache.linkis.engineconnplugin.seatunnel.util.SeatunnelUtils.{
  generateExecFile,
  localArray
}
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadInstanceResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse

import java.io.File
import java.nio.file.Files
import java.util
import java.util.concurrent.{Future, TimeUnit}

import scala.collection.JavaConverters._

class SeatunnelFlinkSQLOnceCodeExecutor(
    override val id: Long,
    override protected val seatunnelEngineConnContext: SeatunnelEngineConnContext
) extends SeatunnelOnceExecutor
    with OperableOnceExecutor {
  private var params: util.Map[String, String] = _
  private var future: Future[_] = _
  private var daemonThread: Future[_] = _
  var isFailed = false

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = {
    val code: String = options(TaskConstant.CODE)
    params = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent
      .asInstanceOf[util.Map[String, String]]
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        logger.info("Try to execute codes." + code)
        if (runCode(code) != 0) {
          isFailed = true
          setResponse(
            ErrorExecuteResponse(
              "Run code failed!",
              new JobExecutionException(EXEC_FLINKSQL_CODE_ERROR.getErrorDesc)
            )
          )
          tryFailed()
        }
        logger.info("All codes completed, now stop SeatunnelEngineConn.")
        closeDaemon()
        if (!isFailed) {
          trySucceed()
        }
        this synchronized notify()
      }
    })
  }

  protected def runCode(code: String): Int = {
    logger.info("Execute SeatunnelFlink Process")

    var args: Array[String] = Array.empty
    val flinkRunMode = LINKIS_FLINK_RUNMODE.getValue
    if (params != null && params.containsKey(flinkRunMode)) {
      val config = LINKIS_FLINK_CONFIG.getValue
      val variable = LINKIS_FLINK_VARIABLE.getValue
      val check = LINKIS_FLINK_CHECK.getValue

      args = Array(
        GET_LINKIS_FLINK_RUNMODE,
        params.getOrDefault(flinkRunMode, "run"),
        GET_LINKIS_FLINK_CHECK,
        params.getOrDefault(check, "false"),
        GET_LINKIS_FLINK_CONFIG,
        generateExecFile(code)
      )

      if (params.containsKey(variable)) {
        val variableMap = GSON.fromJson(params.get(variable), classOf[util.HashMap[String, String]])
        variableMap.asScala.foreach(f => {
          args ++ Array(GET_LINKIS_FLINK_VARIABLE, s"${f._1}=${f._2}")
        })
      }

    } else {
      args = localArray(code)
    }
    System.setProperty("SEATUNNEL_HOME", System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue))
    Files.createSymbolicLink(
      new File(System.getenv(ENGINE_CONN_LOCAL_PATH_PWD_KEY.getValue) + "/seatunnel").toPath,
      new File(SeatunnelEnvConfiguration.SEATUNNEL_HOME.getValue).toPath
    )
    logger.info(s"Execute SeatunnelFlinkSQL Process end args:${args.mkString(" ")}")
    LinkisSeatunnelFlinkSQLClient.main(args)
  }

  override protected def waitToRunning(): Unit = {
    if (!isCompleted) {
      daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(
        new Runnable {
          override def run(): Unit = {
            if (!(future.isDone || future.isCancelled)) {
              logger.info("The SeatunnelFlinkSQL Process In Running")
            }
          }
        },
        SeatunnelEnvConfiguration.SEATUNNEL_STATUS_FETCH_INTERVAL.getValue.toLong,
        SeatunnelEnvConfiguration.SEATUNNEL_STATUS_FETCH_INTERVAL.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
    }
  }

  override def getCurrentNodeResource(): NodeResource = {
    val resource = new CommonNodeResource
    resource.setUsedResource(
      NodeResourceUtils
        .applyAsLoadInstanceResource(EngineConnObject.getEngineCreationContext.getOptions)
    )
    resource
  }

  protected def closeDaemon(): Unit = {
    if (daemonThread != null) daemonThread.cancel(true)
  }

  override def getProgress: Float = 0f

  override def getProgressInfo: Array[JobProgressInfo] = {
    Array.empty[JobProgressInfo]
  }

  override def getMetrics: util.Map[String, Any] = {
    new util.HashMap[String, Any]()
  }

  override def getDiagnosis: util.Map[String, Any] = new util.HashMap[String, Any]()
}
