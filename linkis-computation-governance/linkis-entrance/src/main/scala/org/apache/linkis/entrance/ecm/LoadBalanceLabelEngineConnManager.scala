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
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.engine.ReuseExclusionLabel
import org.apache.linkis.manager.label.entity.entrance.{BindEngineLabel, LoadBalanceLabel}
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.entity._
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.EngineConnExecutor
import org.apache.linkis.server.BDPJettyServerHelper

import java.util
import java.util.Random

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class LoadBalanceLabelEngineConnManager extends ComputationEngineConnManager with Logging {

  private val markReqAndMarkCache = new util.HashMap[MarkReq, util.List[Mark]]()
  private val idToMarkCache = new util.HashMap[String, Mark]()

  private def getMarkReqAndMarkCache(): util.Map[MarkReq, util.List[Mark]] = markReqAndMarkCache

  private def getIdToMarkCache(): util.Map[String, Mark] = idToMarkCache

  private val MARK_REQ_CACHE_LOCKER = new Object()

  private def getMarkNumByMarReq(markReq: MarkReq): Int = {
    if (markReq.getLabels.containsKey(LabelKeyConstant.LOAD_BALANCE_KEY)) {
      val loadBalanceLabel = MarkReq.getLabelBuilderFactory.createLabel[LoadBalanceLabel](
        LabelKeyConstant.LOAD_BALANCE_KEY,
        markReq.getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY)
      )
      if (loadBalanceLabel.getCapacity > 0) {
        loadBalanceLabel.getCapacity
      } else {
        ECMPluginConf.DEFAULT_LOADBALANCE_CAPACITY.getValue
      }
    } else {
      logger.error(
        s"There must be LoadBalanceLabel in markReq : ${BDPJettyServerHelper.gson.toJson(markReq)}"
      )
      ECMPluginConf.DEFAULT_LOADBALANCE_CAPACITY.getValue
    }
  }

  /**
   * Apply for a mark
   *   1. If there is no corresponding mark, a new one will be generated. 2. A markrequest
   *      corresponds to multiple marks, and a mark corresponds to an engine. 3 If there is a
   *      bindenginelabel, you need to randomly select one at jobstart and cache it for subsequent
   *      jobgroups. Delete the cache at jobend. 4. Return mark
   *
   * @param markReq
   * @return
   */
  override def applyMark(markReq: MarkReq): Mark = {
    if (null == markReq) return null
    val markNum: Int = getMarkNumByMarReq(markReq)
    var count = 0
    if (getMarkReqAndMarkCache().containsKey(markReq)) {
      count = getMarkReqAndMarkCache().get(markReq).size()
    }
    while (count < markNum) {
      createMark(markReq)
      count = getMarkReqAndMarkCache().get(markReq).size()
    }
    val markList = getMarkReqAndMarkCache().get(markReq)
    var chooseMark: Mark = null
    if (markReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
      val bindEngineLabel = MarkReq.getLabelBuilderFactory.createLabel[BindEngineLabel](
        LabelKeyConstant.BIND_ENGINE_KEY,
        markReq.getLabels.get(LabelKeyConstant.BIND_ENGINE_KEY)
      )
      if (bindEngineLabel.getIsJobGroupHead) {
        chooseMark = markList.get(new util.Random().nextInt(markList.asScala.length))
        getIdToMarkCache().put(bindEngineLabel.getJobGroupId, chooseMark)
      } else if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
        chooseMark = getIdToMarkCache().get(bindEngineLabel.getJobGroupId)
        val insList = getMarkCache().get(chooseMark)
        if (null == insList || insList.size() != 1) {
          val msg =
            s"Engine instance releated to chooseMark : ${BDPJettyServerHelper.gson.toJson(chooseMark)} with bindEngineLabel : ${bindEngineLabel.getStringValue} cannot be null"
          logger.error(msg)
          throw new ECMPluginErrorException(ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE, msg)
        }
      } else {
        val msg =
          s"Cannot find mark${chooseMark.getMarkId()} related to bindEngineLabel : ${bindEngineLabel.getStringValue}"
        logger.error(msg)
        throw new ECMPluginErrorException(ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE, msg)
      }
      if (bindEngineLabel.getIsJobGroupEnd) {
        if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
          logger.info(
            s"Start to remove mark${chooseMark.getMarkId()} Cache ${bindEngineLabel.getStringValue}"
          )
          getIdToMarkCache().remove(bindEngineLabel.getJobGroupId)
        } else {
          logger.error(
            s"Cannot find mark${chooseMark.getMarkId()} related to bindEngineLabel : ${bindEngineLabel.getStringValue}, cannot remove it."
          )
        }
      }
    } else {
      chooseMark = markList.get(new Random().nextInt(count))
    }
    chooseMark
  }

  override def createMark(markReq: MarkReq): Mark = {
    val mark = new LoadBalanceMark(nextMarkId(), markReq)
    addMark(mark, new util.ArrayList[ServiceInstance]())
    addMarkReqAndMark(markReq, mark)
    mark
  }

  private def addMarkReqAndMark(req: MarkReq, mark: DefaultMark): Unit = {
    if (null != req) MARK_REQ_CACHE_LOCKER.synchronized {
      val markList = getMarkReqAndMarkCache().get(req)
      if (null != markList) {
        val mayBeMark = markList.asScala.find(_.getMarkId().equals(mark.getMarkId())).orNull
        if (null == mayBeMark) {
          markList.add(mark)
        } else {
          // todo check if need to update labels in mark
        }
      } else {
        val markList = new util.ArrayList[Mark]()
        markList.add(mark)
        getMarkReqAndMarkCache().put(req, markList)
      }
    }
  }

  override def getAvailableEngineConnExecutor(
      mark: Mark,
      execTask: CodeLogicalUnitExecTask
  ): EngineConnExecutor = {
    if (null != mark && getMarkCache().containsKey(mark)) {
      tryReuseEngineConnExecutor(mark) match {
        case Some(engineConnExecutor) =>
          logger.info(s"mark ${mark.getMarkId()} ReuseEngineConnExecutor $engineConnExecutor")
          return engineConnExecutor
        case None =>
      }
      logger.info(s"mark ${mark.getMarkId()} start to askEngineConnExecutor")
      val engineConnAskReq = mark.getMarkReq.createEngineConnAskReq()
      val existInstances = getAllInstances()
      if (null != existInstances && existInstances.nonEmpty) {
        val reuseExclusionLabel =
          MarkReq.getLabelBuilderFactory.createLabel(classOf[ReuseExclusionLabel])
        reuseExclusionLabel.setInstances(existInstances.mkString(";"))
        engineConnAskReq.getLabels.put(
          LabelKeyConstant.REUSE_EXCLUSION_KEY,
          reuseExclusionLabel.getValue
        )
      }
      val engineConnExecutor = askEngineConnExecutor(engineConnAskReq, mark, execTask)
      saveToMarkCache(mark, engineConnExecutor)
      logger.debug(
        s"mark ${mark.getMarkId()} Finished to  getAvailableEngineConnExecutor by create"
      )
      engineConnExecutor
    } else {
      throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"mark cannot be null")
    }
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
      logger.info(
        s" Start to release all mark relation to serviceInstance ${engineConnExecutor.getServiceInstance}"
      )
      getMarksByInstance(engineConnExecutor.getServiceInstance).foreach(
        releaseMarkAndServiceInstance(_, engineConnExecutor.getServiceInstance)
      )
    }
  }

  private def releaseMarkAndServiceInstance(mark: Mark, serviceInstance: ServiceInstance): Unit = {
    logger.info(
      s" Start to release mark${mark.getMarkId()} relation to serviceInstance $serviceInstance"
    )
    val instances = getInstances(mark)
    if (null != instances) {
      instances.remove(serviceInstance)
      if (instances.isEmpty) releaseMark(mark)
    }
    if (!getMarkCache().containsKey(mark)) MARK_REQ_CACHE_LOCKER.synchronized {
      val marks = getMarkReqAndMarkCache().get(mark.getMarkReq)
      if (null == marks || marks.isEmpty) {
        getMarkReqAndMarkCache().remove(mark.getMarkReq)
      } else {
        val newMarks = marks.asScala.filter(!_.getMarkId().equals(mark.getMarkId()))
        if (null == newMarks || newMarks.isEmpty) {
          getMarkReqAndMarkCache().remove(mark.getMarkReq)
        } else {
          getMarkReqAndMarkCache().put(mark.getMarkReq, newMarks.asJava)
        }
      }
    }
  }

  protected def getAllInstances(): Array[String] = {
    val instances = new ArrayBuffer[String]
    getMarkCache()
      .values()
      .asScala
      .foreach(_.asScala.foreach(s => instances.append(s.getInstance)))
    instances.toArray
  }

  override def getPolicy(): Policy = Policy.Label

}
