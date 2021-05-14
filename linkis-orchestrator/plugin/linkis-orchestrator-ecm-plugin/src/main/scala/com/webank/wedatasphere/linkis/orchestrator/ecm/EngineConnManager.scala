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
 */

package com.webank.wedatasphere.linkis.orchestrator.ecm

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineAskRequest
import com.webank.wedatasphere.linkis.orchestrator.ecm.conf.ECMPluginConf
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity.{Mark, MarkReq, Policy}
import com.webank.wedatasphere.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineConnExecutor

import scala.collection.JavaConversions._


trait EngineConnManager {

  /**
    * 申请获取一个Mark
    * 1. 如果没有对应的Mark就生成新的
    * 2. 生成新的Mark会存在请求引擎的过程，如果请求到了则存入Map中：Mark为Key，EngineConnExecutor为Value
    * 3. 将Mark进行返回
    *
    * @param markReq
    * @return
    */
  def applyMark(markReq: MarkReq): Mark


  /**
    * 通过Mark向缓存中获取一个可用的EngineConnExecutor
    *
    * @param mark
    * @return
    */
  def getAvailableEngineConnExecutor(mark: Mark): EngineConnExecutor


  /**
    * 移除和该Mark相关的engineConn
    * 释放锁等信息
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

  private val markCache = new util.HashMap[Mark, util.List[ServiceInstance]]()

  private val MARK_CACHE_LOCKER = new Object()

  override def setEngineConnApplyAttempts(attemptNumber: Int): Unit = this.attemptNumber = attemptNumber

  override def getEngineConnApplyAttempts(): Int = this.attemptNumber

  override def getParallelism(): Int = this.parallelism

  override def setParallelism(parallelism: Int): Unit = this.parallelism = parallelism

  override def getEngineConnApplyTime: Long = this.timeOut

  override def setEngineConnApplyTime(applyTime: Long): Unit = this.timeOut = applyTime

  override def getEngineConnExecutorCache(): util.Map[ServiceInstance, EngineConnExecutor] = engineConnExecutorCache

  override def getMarkCache(): util.Map[Mark, util.List[ServiceInstance]] = markCache

  override def getAvailableEngineConnExecutor(mark: Mark): EngineConnExecutor = {
    info(s"mark ${mark.getMarkId()} start to  getAvailableEngineConnExecutor")
    if (null != mark && getMarkCache().containsKey(mark)) {
      val instances = getInstances(mark)
      if (null != instances) {
        val executors = Utils.tryAndWarn {
          instances.map(getEngineConnExecutorCache().get(_)).filter(null != _).sortBy { executor =>
            if (null == executor.getRunningTaskCount) {
              0
            } else {
              executor.getRunningTaskCount
            }
          }
        }
        if (null != executors && executors.nonEmpty) {
          for (executor <- executors) {
            if (executor.useEngineConn) {
              info(s"mark ${mark.getMarkId()} Finished to   getAvailableEngineConnExecutor by reuse")
              return executor
            }
          }
        }
      }

      val engineConnExecutor = askEngineConnExecutor(mark.getMarkReq.createEngineConnAskReq())
      engineConnExecutor.useEngineConn
      getEngineConnExecutorCache().put(engineConnExecutor.getServiceInstance, engineConnExecutor)
      if (null == getInstances(mark)) {
        addMark(mark, new util.ArrayList[ServiceInstance]())
      }
      getMarkCache().get(mark).add(engineConnExecutor.getServiceInstance)
      info(s"mark ${mark.getMarkId()} Finished to  getAvailableEngineConnExecutor by create")
      engineConnExecutor
    } else {
      throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, " mark cannot null")
    }
  }

  protected def addMark(mark: Mark, instances: util.List[ServiceInstance]): Unit = MARK_CACHE_LOCKER.synchronized {
    if (null != mark && !getMarkCache().containsKey(mark)) {
      getMarkCache().put(mark, instances)
    }
  }

  protected def getInstances(mark: Mark): util.List[ServiceInstance] = MARK_CACHE_LOCKER.synchronized {
    if (null != mark && getMarkCache().containsKey(mark)) {
      getMarkCache().get(mark)
    } else {
      null
    }
  }

  override def releaseEngineConnExecutor(engineConnExecutor: EngineConnExecutor, mark: Mark): Unit = {
    if (null != engineConnExecutor && null != mark && getMarkCache().containsKey(mark)) {
      info(s"Start to release EngineConnExecutor mark id ${mark.getMarkId()} engineConnExecutor ${engineConnExecutor.getServiceInstance}")
      getEngineConnExecutorCache().remove(engineConnExecutor.getServiceInstance)
      engineConnExecutor.close()
      val instances = getInstances(mark)
      if (null != instances) {
        instances.remove(engineConnExecutor.getServiceInstance)
        if (instances.isEmpty) releaseMark(mark)
      }
    }
  }

  protected def askEngineConnExecutor(engineAskRequest: EngineAskRequest): EngineConnExecutor

  override def releaseMark(mark: Mark): Unit = {
    if (null != mark && getMarkCache().containsKey(mark)) {
      info(s"Start to release mark ${mark.getMarkId()}")
      val executors = getMarkCache().get(mark).map(getEngineConnExecutorCache().get(_))
      Utils.tryAndError(executors.foreach { executor =>
        getEngineConnExecutorCache().remove(executor.getServiceInstance)
        executor.close()
      })
      removeMark(mark)
      info(s"Finished to release mark ${mark.getMarkId()}")
    }
  }

  protected def removeMark(mark: Mark): Unit = MARK_CACHE_LOCKER.synchronized {
    if (null != mark && getMarkCache().containsKey(mark)) {
      getMarkCache().remove(mark)
    }
  }

}