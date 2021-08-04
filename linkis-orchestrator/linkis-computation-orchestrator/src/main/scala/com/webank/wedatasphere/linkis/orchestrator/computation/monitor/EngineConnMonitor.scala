/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.orchestrator.computation.monitor

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.governance.common.entity.NodeExistStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.engineconn.{RequestEngineStatusBatch, ResponseEngineStatusBatch}
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import com.webank.wedatasphere.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import com.webank.wedatasphere.linkis.orchestrator.computation.execute.CodeExecTaskExecutor
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineConnExecutor
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.server.{BDPJettyServerHelper, toJavaMap}

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaSetConverter, mapAsScalaMapConverter}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object EngineConnMonitor extends Logging {

  private[linkis] def addEngineExecutorStatusMonitor(engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]],
                                                     endJobByEngineInstance: ServiceInstance => Unit): Unit = {
    val task = new Runnable {
      override def run(): Unit = {
        val startTime = System.currentTimeMillis()
        val engineExecutorCache = engineConnExecutorCache
        val unActivityEngines = Collections.synchronizedSet(new util.HashSet[EngineConnExecutor]())
        engineExecutorCache.values.foreach(taskExecutors => taskExecutors.foreach(executor => {
          if (startTime - executor.getEngineConnExecutor.getLastUpdateTime() > ComputationOrchestratorConf.ENGINECONN_LASTUPDATE_TIMEOUT.getValue.toLong) {
            unActivityEngines.add(executor.getEngineConnExecutor)
          }
        }))
        if (null != unActivityEngines && !unActivityEngines.isEmpty) {
          info(s"There are ${unActivityEngines.size} unActivity engines.")
          val engineList = new util.ArrayList[ServiceInstance]()
          unActivityEngines.asScala.foreach(engine => {
            engineList.add(engine.getServiceInstance)
            if (engineList.size() >= GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
              queryEngineStatusAndHandle(engineConnExecutorCache, engineList, endJobByEngineInstance)
              engineList.clear()
            }
          })
          if (!engineList.isEmpty) {
            queryEngineStatusAndHandle(engineConnExecutorCache, engineList, endJobByEngineInstance)
            engineList.clear()
          }
        }
        val endTime = System.currentTimeMillis()
        if (endTime - startTime >= ComputationOrchestratorConf.ENGINECONN_ACTIVITY_MONITOR_INTERVAL.getValue.toLong) {
          warn("Query engines status costs longer time than query task interval, you should increase interval.")
        }
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(task, 10000, ComputationOrchestratorConf.ENGINECONN_ACTIVITY_MONITOR_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
    info("Entrance engineStatusMonitor inited.")
  }

  private def queryEngineStatusAndHandle(engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]],
                                         engineList: util.List[ServiceInstance],
                                         endJobByEngineInstance: ServiceInstance => Unit): Unit = {
    val requestEngineStatus = RequestEngineStatusBatch(engineList)
    Utils.tryAndError {
      Sender.getSender(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue).ask(requestEngineStatus) match {
        case response: ResponseEngineStatusBatch =>
          if (null != response.msg) {
            info(s"ResponseEngineStatusBatch msg : ${response.msg}")
          }
          if (response.engineStatus.size() != requestEngineStatus.engineList.size()) {
            error(s"ResponseEngineStatusBatch engines size : ${response.engineStatus.size()} is not euqal requet : ${requestEngineStatus.engineList.size()}.")
            val unKnownEngines = new ArrayBuffer[ServiceInstance]()
            requestEngineStatus.engineList.asScala.foreach(instance => {
              if (!response.engineStatus.containsKey(instance)) {
                response.engineStatus.put(instance, NodeExistStatus.Unknown)
                unKnownEngines += instance
              }
            })
            val instances = unKnownEngines.map(_.getInstance).mkString(",")
            warn(s"These engine instances cannot be found in manager : ${instances}")
          }
          response.engineStatus.asScala.foreach(status => {
            status._2 match {
              case NodeExistStatus.UnExist =>
                warn(s"Engine ${status._1} is Failed, now go to clear its task.")
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
                            debug(s"Will update engineConnExecutor(${status._1}) lastupdated time")
                          }
                          updateExecutorActivityTime(status._1, engineConnExecutorCache)
                        }
                      case o: Any =>
                        Utils.tryAndWarn(warn(s"Unknown response : ${BDPJettyServerHelper.gson.toJson(o)} for request : ${BDPJettyServerHelper.gson.toJson(requestNodeStatus)}"))
                    }
                  } {
                    case t: Throwable =>
                      error(s"Failed to get status of engineConn : ${status._1}, now end the job. ", t)
                      endJobByEngineInstance(status._1)
                  }
                }
              case o: Any =>
                error(s"Status of engine ${status._1.toString} is ${status._2}")
            }
          })
        case _ =>
          error(s"Invalid response. request : ${BDPJettyServerHelper.gson.toJson(requestEngineStatus)}")
      }
    }
  }

  private def updateExecutorActivityTime(serviceInstance: ServiceInstance, engineConnExecutorCache: mutable.HashMap[ServiceInstance, Array[CodeExecTaskExecutor]]): Unit = {
    if (null != serviceInstance) {
      val taskExecutorList = engineConnExecutorCache.getOrDefault(serviceInstance, null)
      if (null != taskExecutorList) {
        val executors = taskExecutorList.filter(_.getEngineConnExecutor.getServiceInstance.equals(serviceInstance))
        if (null != executors && executors.size >= 1) {
          synchronized {
            executors.head.getEngineConnExecutor.updateLastUpdateTime()
          }
        } else {
          warn(s"EngineConnExecutor ${serviceInstance.toString} cannot be found in engineConnExecutorCache")
        }
      } else {
        warn(s"EngineConnExecutor ${serviceInstance.toString} cannot be found in engineConnExecutorCache")
      }
    }
  }

}
