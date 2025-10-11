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

package org.apache.linkis.orchestrator.computation.monitor

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.{ExecutionNodeStatus, NodeExistStatus}
import org.apache.linkis.governance.common.protocol.engineconn.{
  RequestEngineStatusBatch,
  ResponseEngineStatusBatch
}
import org.apache.linkis.governance.common.utils.GovernanceConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.execute.{CodeExecTaskExecutor, EngineConnTaskInfo}
import org.apache.linkis.orchestrator.listener.task.{
  TaskErrorResponseEvent,
  TaskLogEvent,
  TaskStatusEvent
}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.{toJavaMap, BDPJettyServerHelper}

import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object EngineConnMonitor extends Logging {

  private val ENGINECONN_LASTUPDATE_TIMEOUT =
    ComputationOrchestratorConf.ENGINECONN_LASTUPDATE_TIMEOUT.getValue.toLong

  private[linkis] def addEngineExecutorStatusMonitor(
      engineConnExecutorCache: util.Map[EngineConnTaskInfo, CodeExecTaskExecutor]
  ): Unit = {
    val task = new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val startTime = System.currentTimeMillis()
        val unActivityExecutors =
          new mutable.HashMap[ServiceInstance, ArrayBuffer[CodeExecTaskExecutor]]()
        val allTaskExecutors = new ArrayBuffer[CodeExecTaskExecutor]()
        allTaskExecutors.appendAll(engineConnExecutorCache.values().asScala)
        allTaskExecutors
          .filter(executor =>
            (startTime - executor.getEngineConnExecutor
              .getLastUpdateTime()) > ENGINECONN_LASTUPDATE_TIMEOUT
          )
          .foreach { executor =>
            val executors = unActivityExecutors.getOrElseUpdate(
              executor.getEngineConnExecutor.getServiceInstance,
              new ArrayBuffer[CodeExecTaskExecutor]()
            )
            executors.append(executor)
          }

        if (unActivityExecutors.nonEmpty) {
          logger.info("There are {} unActivity engineConn.", unActivityExecutors.size)

          if (unActivityExecutors.size > GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
            queryEngineStatusAndHandle(unActivityExecutors, unActivityExecutors.keys.toList.asJava)
          } else {
            val engineList = new util.ArrayList[ServiceInstance]()
            unActivityExecutors.keys.foreach(serviceInstance => {
              engineList.add(serviceInstance)
              if (engineList.size() >= GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
                queryEngineStatusAndHandle(unActivityExecutors, engineList)
                engineList.clear()
              }
            })
            if (!engineList.isEmpty) {
              queryEngineStatusAndHandle(unActivityExecutors, engineList)
              engineList.clear()
            }
          }
        }
        val endTime = System.currentTimeMillis()
        if (
            endTime - startTime >= ComputationOrchestratorConf.ENGINECONN_ACTIVITY_MONITOR_INTERVAL.getValue.toLong
        ) {
          logger.warn(
            "Query engines status costs longer time than query task interval, you should increase interval."
          )
        }
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(
      task,
      10000,
      ComputationOrchestratorConf.ENGINECONN_ACTIVITY_MONITOR_INTERVAL.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
    logger.info("Entrance engineStatusMonitor inited.")
  }

  private def queryEngineStatusAndHandle(
      unActivityExecutors: mutable.HashMap[ServiceInstance, ArrayBuffer[CodeExecTaskExecutor]],
      engineList: util.List[ServiceInstance]
  ): Unit = {
    val requestEngineStatus = RequestEngineStatusBatch(engineList)
    Utils.tryAndError {
      Sender
        .getSender(GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue)
        .ask(requestEngineStatus) match {
        case response: ResponseEngineStatusBatch =>
          if (null != response.msg) {
            logger.info("ResponseEngineStatusBatch msg: {}", response.msg)
          }
          if (response.engineStatus.size() != requestEngineStatus.engineList.size()) {
            logger.warn(
              "ResponseEngineStatusBatch engines size: {} is not equal request: {}.",
              response.engineStatus.size(): Any,
              requestEngineStatus.engineList.size(): Any
            )
            val unKnownEngines = new ArrayBuffer[ServiceInstance]()
            requestEngineStatus.engineList.asScala.foreach(instance => {
              if (!response.engineStatus.containsKey(instance)) {
                response.engineStatus.put(instance, NodeExistStatus.Unknown)
                unKnownEngines += instance
              }
            })
            val instances = unKnownEngines.map(_.getInstance).mkString(",")
            logger.warn("These engine instances cannot be found in manager : {}", instances)
          }
          response.engineStatus.asScala.foreach(status =>
            dealWithEngineStatus(status, unActivityExecutors)
          )
        case _ =>
          logger.warn(
            "Invalid response. request : {}",
            BDPJettyServerHelper.gson.toJson(requestEngineStatus)
          )
      }
    }
  }

  private def dealWithEngineStatus(
      status: (ServiceInstance, NodeExistStatus),
      unActivityExecutors: mutable.HashMap[ServiceInstance, ArrayBuffer[CodeExecTaskExecutor]]
  ): Unit = {
    status._2 match {
      case NodeExistStatus.UnExist =>
        logger.warn("Engine {} is Failed, now go to clear its task.", status._1)
        killTask(unActivityExecutors.get(status._1))
      case NodeExistStatus.Unknown =>
        val engineConnExecutor = unActivityExecutors.getOrDefault(status._1, null)
        if (null != engineConnExecutor) {
          Utils.tryCatch {
            val requestNodeStatus = new RequestNodeStatus
            Sender.getSender(status._1).ask(requestNodeStatus) match {
              case rs: ResponseNodeStatus =>
                if (NodeStatus.isCompleted(rs.getNodeStatus)) {
                  killTask(unActivityExecutors.get(status._1))
                } else {
                  if (logger.isDebugEnabled()) {
                    logger.debug("Will update engineConnExecutor({}) lastupdated time", status._1)
                  }
                  updateExecutorActivityTime(status._1, unActivityExecutors)
                }
              case _ =>
                logger.warn(
                  s"Unknown response for request : ${BDPJettyServerHelper.gson.toJson(requestNodeStatus)}"
                )
            }
          } { t: Throwable =>
            logger.error(s"Failed to get status of engineConn : ${status._1}, now end the job. ", t)
            killTask(unActivityExecutors.get(status._1))
          }
        }
      case _ =>
    }
  }

  private def killTask(mayExecutors: Option[ArrayBuffer[CodeExecTaskExecutor]]): Unit = {
    if (mayExecutors.isEmpty) {
      logger.error("executor is not Defined")
      return
    }
    val executors = mayExecutors.get
    executors.foreach { executor =>
      val execTask = executor.getExecTask
      Utils.tryAndError {
        val labels: Array[Label[_]] = executor.getEngineConnExecutor.getLabels()
        val engineType: String = LabelUtil.getEngineTypeLabel(labels.toList.asJava).getEngineType
        logger.warn(
          s"Will kill task ${execTask.getIDInfo()} because the engine ${executor.getEngineConnExecutor.getServiceInstance.toString} quited unexpectedly."
        )
        val errLog = LogUtils.generateERROR(
          s"Your job : ${execTask.getIDInfo()} was failed because the ${engineType} engine quitted unexpectedly(任务${execTask
            .getIDInfo()}失败，" +
            s"原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM)."
        )
        val logEvent = TaskLogEvent(execTask, errLog)
        execTask.getPhysicalContext.pushLog(logEvent)
        val errorResponseEvent = TaskErrorResponseEvent(
          execTask,
          "task failed，Engine quitted unexpectedly(任务运行失败原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM)."
        )
        execTask.getPhysicalContext.broadcastSyncEvent(errorResponseEvent)
        val statusEvent = TaskStatusEvent(execTask, ExecutionNodeStatus.Failed)
        execTask.getPhysicalContext.broadcastSyncEvent(statusEvent)
      }
    }
  }

  private def updateExecutorActivityTime(
      serviceInstance: ServiceInstance,
      engineConnExecutorCache: mutable.HashMap[ServiceInstance, ArrayBuffer[CodeExecTaskExecutor]]
  ): Unit = {
    if (null != serviceInstance) {
      val executors = engineConnExecutorCache.getOrDefault(serviceInstance, null)
      if (null != executors) {
        executors.foreach { executor =>
          if (executor.getEngineConnExecutor.getServiceInstance.equals(serviceInstance)) {
            executor.getEngineConnExecutor.updateLastUpdateTime()
          }
        }
      } else {
        logger.warn(
          "EngineConnExecutor {} cannot be found in engineConnExecutorCache",
          serviceInstance.toString
        )
      }
    }
  }

}
