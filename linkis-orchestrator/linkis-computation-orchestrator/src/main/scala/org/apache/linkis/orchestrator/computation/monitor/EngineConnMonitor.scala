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
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.NodeExistStatus
import org.apache.linkis.governance.common.protocol.engineconn.{
  RequestEngineStatusBatch,
  ResponseEngineStatusBatch
}
import org.apache.linkis.governance.common.utils.GovernanceConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.execute.CodeExecTaskExecutor
import org.apache.linkis.orchestrator.ecm.service.EngineConnExecutor
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.{toJavaMap, BDPJettyServerHelper}

import java.util
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters.{asScalaBufferConverter, mapAsScalaMapConverter}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object EngineConnMonitor extends Logging {

  private val ENGINECONN_LASTUPDATE_TIMEOUT =
    ComputationOrchestratorConf.ENGINECONN_LASTUPDATE_TIMEOUT.getValue.toLong

  private[linkis] def addEngineExecutorStatusMonitor(
      engineConnExecutorCache: util.Map[ServiceInstance, Array[CodeExecTaskExecutor]],
      endJobByEngineInstance: ServiceInstance => Unit
  ): Unit = {
    val task = new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val startTime = System.currentTimeMillis()
        val engineExecutorCache = engineConnExecutorCache
        val unActivityEngines = new mutable.HashSet[EngineConnExecutor]()
        val allTaskExecutors = engineExecutorCache.values().iterator()
        while (allTaskExecutors.hasNext) {
          val taskExecutors = allTaskExecutors.next()
          taskExecutors.foreach(executor => {
            val interval = startTime - executor.getEngineConnExecutor.getLastUpdateTime()
            if (interval > ENGINECONN_LASTUPDATE_TIMEOUT) {
              unActivityEngines.add(executor.getEngineConnExecutor)
            }
          })
        }
        if (null != unActivityEngines && unActivityEngines.nonEmpty) {
          logger.info("There are {} unActivity engines.", unActivityEngines.size)
          val engineList = new util.ArrayList[ServiceInstance]()
          val instanceAndExecutors =
            new mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]]()
          unActivityEngines.foreach(engine => {
            engineList.add(engine.getServiceInstance)
            val executors = engineConnExecutorCache.get(engine.getServiceInstance)
            if (null != executors) {
              instanceAndExecutors.put(engine.getServiceInstance, executors)
            }
            if (engineList.size() >= GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
              queryEngineStatusAndHandle(instanceAndExecutors, engineList, endJobByEngineInstance)
              engineList.clear()
            }
          })
          if (!engineList.isEmpty) {
            queryEngineStatusAndHandle(instanceAndExecutors, engineList, endJobByEngineInstance)
            engineList.clear()
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
      engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]],
      engineList: util.List[ServiceInstance],
      endJobByEngineInstance: ServiceInstance => Unit
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
            dealWithEngineStatus(status, engineConnExecutorCache, endJobByEngineInstance)
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
      engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]],
      endJobByEngineInstance: ServiceInstance => Unit
  ): Unit = {
    status._2 match {
      case NodeExistStatus.UnExist =>
        logger.warn("Engine {} is Failed, now go to clear its task.", status._1)
        endJobByEngineInstance(status._1)
      case NodeExistStatus.Exist | NodeExistStatus.Unknown =>
        val engineConnExecutor = engineConnExecutorCache.getOrDefault(status._1, null)
        if (null != engineConnExecutor) {
          Utils.tryCatch {
            // todo check - only for engine with accessible executor
            val requestNodeStatus = new RequestNodeStatus
            Sender.getSender(status._1).ask(requestNodeStatus) match {
              case rs: ResponseNodeStatus =>
                if (NodeStatus.isCompleted(rs.getNodeStatus)) {
                  endJobByEngineInstance(status._1)
                } else {
                  if (logger.isDebugEnabled()) {
                    logger.debug("Will update engineConnExecutor({}) lastupdated time", status._1)
                  }
                  updateExecutorActivityTime(status._1, engineConnExecutorCache)
                }
              case _ =>
                logger.warn(
                  s"Unknown response for request : ${BDPJettyServerHelper.gson.toJson(requestNodeStatus)}"
                )
            }
          } { t: Throwable =>
            logger.error(s"Failed to get status of engineConn : ${status._1}, now end the job. ", t)
            endJobByEngineInstance(status._1)
          }
        }
    }
  }

  private def updateExecutorActivityTime(
      serviceInstance: ServiceInstance,
      engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]]
  ): Unit = {
    if (null != serviceInstance) {
      val taskExecutorList = engineConnExecutorCache.getOrDefault(serviceInstance, null)
      if (null != taskExecutorList) {
        taskExecutorList.foreach(executor =>
          if (executor.getEngineConnExecutor.getServiceInstance.equals(serviceInstance)) {
            executor.getEngineConnExecutor.updateLastUpdateTime()
          }
        )
      } else {
        logger.warn(
          "EngineConnExecutor {} cannot be found in engineConnExecutorCache",
          serviceInstance.toString
        )
      }
    }
  }

}
