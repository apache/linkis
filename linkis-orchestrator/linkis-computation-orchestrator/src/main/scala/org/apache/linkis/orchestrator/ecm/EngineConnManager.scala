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

package org.apache.linkis.orchestrator.ecm

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.protocol.engine.EngineAskRequest
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.entity.{Mark, MarkReq, Policy}
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.EngineConnExecutor

import org.apache.commons.collections.CollectionUtils

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 */
trait EngineConnManager {

  /**
   * Apply for a Mark
   *   1. If there is no corresponding Mark, generate a new one 2. Return Mark
   *
   * @param markReq
   * @return
   */
  def applyMark(markReq: MarkReq): Mark

  /**
   *   1. Create a new Mark 2. There will be a process of requesting the engine when generating a
   *      new Mark. If the request is received, it will be stored in the Map: Mark is Key,
   *      EngineConnExecutor is Value
   */
  def createMark(markReq: MarkReq): Mark

  /**
   * Obtain an available EngineConnExecutor from the cache through Mark
   *
   * @param mark
   * @return
   */
  def getAvailableEngineConnExecutor(
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): EngineConnExecutor

  /**
   * Remove the engineConn related to the Mark Release lock and other information
   *
   * @param mark
   */
  def releaseMark(mark: Mark): Unit

  def releaseEngineConnExecutor(engineConnExecutor: EngineConnExecutor, mark: Mark): Unit

  def getEngineConnExecutorCache(): util.Map[ServiceInstance, EngineConnExecutor]

  protected def getMarkCache(): util.Map[Mark, util.List[ServiceInstance]]

  def getPolicy(): Policy

  def setParallelism(parallelism: Int): Unit

  def getParallelism(): Int

  def setEngineConnApplyTime(applyTime: Long): Unit

  def getEngineConnApplyTime: Long

  def setEngineConnApplyAttempts(attemptNumber: Int): Unit

  def getEngineConnApplyAttempts(): Int

}

abstract class AbstractEngineConnManager extends EngineConnManager with Logging {

  private var parallelism: Int = _

  private var timeOut: Long = _

  private var attemptNumber: Int = _

  private val engineConnExecutorCache = new util.HashMap[ServiceInstance, EngineConnExecutor]()

  private val markCache: util.Map[Mark, util.List[ServiceInstance]] =
    new util.concurrent.ConcurrentHashMap[Mark, util.List[ServiceInstance]]()

  override def setEngineConnApplyAttempts(attemptNumber: Int): Unit = this.attemptNumber =
    attemptNumber

  override def getEngineConnApplyAttempts(): Int = this.attemptNumber

  override def getParallelism(): Int = this.parallelism

  override def setParallelism(parallelism: Int): Unit = this.parallelism = parallelism

  override def getEngineConnApplyTime: Long = this.timeOut

  override def setEngineConnApplyTime(applyTime: Long): Unit = this.timeOut = applyTime

  override def getEngineConnExecutorCache(): util.Map[ServiceInstance, EngineConnExecutor] =
    engineConnExecutorCache

  override def getMarkCache(): util.Map[Mark, util.List[ServiceInstance]] = markCache

  override def getAvailableEngineConnExecutor(
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): EngineConnExecutor = {
    logger.info(s"mark ${mark.getMarkId()} start to  getAvailableEngineConnExecutor")
    if (null != mark) {
      tryReuseEngineConnExecutor(mark) match {
        case Some(engineConnExecutor) => return engineConnExecutor
        case None =>
      }
      val engineConnExecutor =
        askEngineConnExecutor(mark.getMarkReq.createEngineConnAskReq(), mark, execTask)
      engineConnExecutor.useEngineConn
      saveToMarkCache(mark, engineConnExecutor)
      logger.debug(
        s"mark ${mark.getMarkId()} Finished to  getAvailableEngineConnExecutor by create"
      )
      engineConnExecutor
    } else {
      throw new ECMPluginErrorException(
        ECMPluginConf.ECM_ERROR_CODE,
        s" mark ${mark.getMarkId()} cannot null"
      )
    }
  }

  protected def tryReuseEngineConnExecutor(mark: Mark): Option[EngineConnExecutor] = {
    val instances = getInstances(mark)
    if (null != instances) {
      val executors = Utils.tryAndWarn {
        instances.asScala.map(getEngineConnExecutorCache().get(_)).filter(null != _).sortBy {
          executor =>
            if (executor.getRunningTaskCount < 0) {
              0
            } else {
              executor.getRunningTaskCount
            }
        }
      }

      if (null != executors && executors.nonEmpty) {
        if (mark.getMarkReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
          // must use the existed engine
          return Some(executors.headOption.get)
        }
        for (executor <- executors) {
          if (executor.useEngineConn) {
            logger.info(
              s"mark ${mark.getMarkId()} Finished to   getAvailableEngineConnExecutor by reuse"
            )
            return Some(executor)
          }
        }
      }
    }
    None
  }

  protected def saveToMarkCache(mark: Mark, engineConnExecutor: EngineConnExecutor) = {
    getEngineConnExecutorCache().put(engineConnExecutor.getServiceInstance, engineConnExecutor)
    if (null == getInstances(mark)) {
      addMark(mark, new util.ArrayList[ServiceInstance]())
    }
    val markedInstances = getMarkCache().get(mark)
    if (markedInstances.isEmpty) {
      markedInstances.add(engineConnExecutor.getServiceInstance)
    } else if (!mark.getMarkReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
      markedInstances.add(engineConnExecutor.getServiceInstance)
    }
  }

  protected def addMark(mark: Mark, instances: util.List[ServiceInstance]): Unit =
    if (null != mark && !getMarkCache().containsKey(mark)) {
      logger.info(s"add mark ${mark.getMarkId()}")
      getMarkCache().put(mark, instances)
    }

  protected def getInstances(mark: Mark): util.List[ServiceInstance] = if (null != mark) {
    getMarkCache().get(mark)
  } else {
    null
  }

  protected def getMarksByInstance(serviceInstance: ServiceInstance): Array[Mark] = {
    val markAndInstance = getMarkCache().entrySet().iterator()
    val buffer = new ArrayBuffer[Mark]()
    while (markAndInstance.hasNext) {
      val next = markAndInstance.next()
      if (next.getValue != null && next.getValue.indexOf(serviceInstance) >= 0) {
        buffer.append(next.getKey)
      }
    }
    buffer.toArray
  }

  override def releaseEngineConnExecutor(
      engineConnExecutor: EngineConnExecutor,
      mark: Mark
  ): Unit = {
    if (null != engineConnExecutor && null != mark && getMarkCache().containsKey(mark)) {
      logger.info(
        s"Start to release EngineConnExecutor mark id ${mark.getMarkId()} engineConnExecutor ${engineConnExecutor.getServiceInstance}"
      )
      getEngineConnExecutorCache().remove(engineConnExecutor.getServiceInstance)
      engineConnExecutor.close()
      val instances = getInstances(mark)
      if (null != instances) {
        instances.remove(engineConnExecutor.getServiceInstance)
        if (instances.isEmpty) releaseMark(mark)
      }
    }
  }

  protected def askEngineConnExecutor(
      engineAskRequest: EngineAskRequest,
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): EngineConnExecutor

  override def releaseMark(mark: Mark): Unit = {
    if (null != mark && getMarkCache().containsKey(mark)) {
      logger.debug(s"Start to release mark ${mark.getMarkId()}")
      val executors = getMarkCache().get(mark).asScala.map(getEngineConnExecutorCache().get(_))
      Utils.tryAndError(executors.foreach { executor =>
        getEngineConnExecutorCache().remove(executor.getServiceInstance)
        executor.close()
      })
      removeMark(mark)
      logger.info(s"Finished to release mark ${mark.getMarkId()}")
    }
  }

  protected def removeMark(mark: Mark): Unit = {
    if (null != mark) {
      getMarkCache().remove(mark)
    }
  }

}
