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
 
package org.apache.linkis.orchestrator.ecm

import java.util
import java.util.Random

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.engine.ReuseExclusionLabel
import org.apache.linkis.manager.label.entity.entrance.{BindEngineLabel, LoadBalanceLabel}
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.entity._
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import org.apache.linkis.orchestrator.ecm.service.EngineConnExecutor
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.commons.collections.CollectionUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class LoadBalanceLabelEngineConnManager extends ComputationEngineConnManager with Logging {


  private val markReqAndMarkCache = new util.HashMap[MarkReq, util.List[Mark]]()
  private val idToMarkCache = new util.HashMap[String, Mark]()

  private def getMarkReqAndMarkCache(): util.Map[MarkReq, util.List[Mark]] = markReqAndMarkCache

  private def getIdToMarkCache(): util.Map[String, Mark] = idToMarkCache

  private val MARK_REQ_CACHE_LOCKER = new Object()

  private def getMarkNumByMarReq(markReq: MarkReq): Int = {
    if (markReq.getLabels.containsKey(LabelKeyConstant.LOAD_BALANCE_KEY)) {
      val loadBalanceLabel = MarkReq.getLabelBuilderFactory.createLabel[LoadBalanceLabel](LabelKeyConstant.LOAD_BALANCE_KEY,
        markReq.getLabels.get(LabelKeyConstant.LOAD_BALANCE_KEY))
      if (loadBalanceLabel.getCapacity > 0) {
        loadBalanceLabel.getCapacity
      } else {
        ECMPluginConf.DEFAULT_LOADBALANCE_CAPACITY.getValue
      }
    } else {
      error(s"There must be LoadBalanceLabel in markReq : ${BDPJettyServerHelper.gson.toJson(markReq)}")
      ECMPluginConf.DEFAULT_LOADBALANCE_CAPACITY.getValue
    }
  }

  /**
   * 申请获取一个Mark
   * 1. 如果没有对应的Mark就生成新的
   * 2. 一个MarkRequest对应多个Mark，一个Mark对应一个Engine
   * 3. 如果存在bindEngineLabel则需要在jobStart的时候随机选择一个，并缓存给后续jobGroup使用，在jobEnd的时候删除缓存
   * 4. 将Mark进行返回
   *
   * @param markReq
   * @return
   */
  override def applyMark(markReq: MarkReq): Mark = {
    if (null == markReq) return null

    val markNum: Int = getMarkNumByMarReq(markReq)
    /*val markReqCache = MARK_REQ_CACHE_LOCKER.synchronized {
      getMarkReqAndMarkCache().keys
    }
    var mayBeMarkReq = markReqCache.find(_.equals(markReq)).orNull*/
    var count = 0
    if (getMarkReqAndMarkCache().containsKey(markReq)) {
      count = getMarkReqAndMarkCache().get(markReq).size()
    }
    // toto check if count >= markNum, will not add more mark
    while (count < markNum) {
      createMark(markReq)
      count = getMarkReqAndMarkCache().get(markReq).size()
    }
    // markReq is in cache, and mark list is ready
    val markList = getMarkReqAndMarkCache().get(markReq)
    var chooseMark: Mark = null
    if (markReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
      val bindEngineLabel = MarkReq.getLabelBuilderFactory.createLabel[BindEngineLabel](LabelKeyConstant.BIND_ENGINE_KEY,
        markReq.getLabels.get(LabelKeyConstant.BIND_ENGINE_KEY))
      if (bindEngineLabel.getIsJobGroupHead) {
        // getRandom mark
        chooseMark = markList.get(new util.Random().nextInt(markList.length))
        //chooseMark.asInstanceOf[LoadBalanceMark].setTaskMarkReq(markReq)
        getIdToMarkCache().put(bindEngineLabel.getJobGroupId, chooseMark)
      } else if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
        chooseMark = getIdToMarkCache().get(bindEngineLabel.getJobGroupId)
        //chooseMark.asInstanceOf[LoadBalanceMark].setTaskMarkReq(markReq)
        val insList = getMarkCache().get(chooseMark)
        if (null == insList || insList.size() != 1) {
          val msg = s"Engine instance releated to chooseMark : ${BDPJettyServerHelper.gson.toJson(chooseMark)} with bindEngineLabel : ${bindEngineLabel.getStringValue} cannot be null"
          error(msg)
          throw new ECMPluginErrorException(ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE, msg)
        }
      } else {
        val msg = s"Cannot find mark${chooseMark.getMarkId()} related to bindEngineLabel : ${bindEngineLabel.getStringValue}"
        error(msg)
        throw new ECMPluginErrorException(ECMPluginConf.ECM_MARK_CACHE_ERROR_CODE, msg)
      }
      if (bindEngineLabel.getIsJobGroupEnd) {
        if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
          info(s"Start to remove mark${chooseMark.getMarkId()} Cache ${bindEngineLabel.getStringValue}")
          getIdToMarkCache().remove(bindEngineLabel.getJobGroupId)
        } else {
          error(s"Cannot find mark${chooseMark.getMarkId()} related to bindEngineLabel : ${bindEngineLabel.getStringValue}, cannot remove it.")
        }
      }
    } else {
      // treat as isHead and isEnd
      chooseMark = markList.get(new Random().nextInt(count))
    }
    chooseMark
  }

  /**
   * 1. 创建一个新的Mark
   * 2. 生成新的Mark会存在请求引擎的过程，如果请求到了则存入Map中：Mark为Key，EngineConnExecutor为Value
   *  3. 生成的Mark数量等于LoadBalance的并发量
   */
  override def createMark(markReq: MarkReq): Mark = {
    val mark = new LoadBalanceMark(nextMarkId(), markReq)
    addMark(mark, new util.ArrayList[ServiceInstance]())
    addMarkReqAndMark(markReq, mark)
    mark
  }

  private def addMarkReqAndMark(req: MarkReq, mark: DefaultMark): Unit = {
    if (null != req)  MARK_REQ_CACHE_LOCKER.synchronized {
      val markList = getMarkReqAndMarkCache().get(req)
      if (null != markList) {
        val mayBeMark = markList.find(_.getMarkId().equals(mark.getMarkId())).orNull
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

  override def getAvailableEngineConnExecutor(mark: Mark): EngineConnExecutor = {

    if (null != mark && getMarkCache().containsKey(mark)) {
      tryReuseEngineConnExecutor(mark) match {
        case Some(engineConnExecutor) =>
          info(s"mark ${mark.getMarkId()} ReuseEngineConnExecutor $engineConnExecutor")
          return engineConnExecutor
        case None =>
    }
      info(s"mark ${mark.getMarkId()} start to askEngineConnExecutor")
      val engineConnAskReq = mark.getMarkReq.createEngineConnAskReq()
      val existInstances = getAllInstances()
      if (null != existInstances && existInstances.nonEmpty) {
        val reuseExclusionLabel = MarkReq.getLabelBuilderFactory.createLabel(classOf[ReuseExclusionLabel])
        reuseExclusionLabel.setInstances(existInstances.mkString(";"))
        engineConnAskReq.getLabels.put(LabelKeyConstant.REUSE_EXCLUSION_KEY, reuseExclusionLabel.getValue)
      }
      val engineConnExecutor = askEngineConnExecutor(engineConnAskReq, mark)
      saveToMarkCache(mark, engineConnExecutor)
      debug(s"mark ${mark.getMarkId()} Finished to  getAvailableEngineConnExecutor by create")
      engineConnExecutor
    } else {
      throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"mark cannot be null")
    }
  }

  override def releaseEngineConnExecutor(engineConnExecutor: EngineConnExecutor, mark: Mark): Unit = {
    if (null != engineConnExecutor && null != mark && getMarkCache().containsKey(mark)) {
      info(s"Start to release EngineConnExecutor mark id ${mark.getMarkId()} engineConnExecutor ${engineConnExecutor.getServiceInstance}")
      getEngineConnExecutorCache().remove(engineConnExecutor.getServiceInstance)
      engineConnExecutor.close()
      info(s" Start to release all mark relation to serviceInstance ${engineConnExecutor.getServiceInstance}")
      getMarksByInstance(engineConnExecutor.getServiceInstance).foreach(releaseMarkAndServiceInstance(_, engineConnExecutor.getServiceInstance))
    }
  }

  private def releaseMarkAndServiceInstance(mark: Mark, serviceInstance: ServiceInstance): Unit = {
    info(s" Start to release mark${mark.getMarkId()} relation to serviceInstance $serviceInstance")
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
        val newMarks = marks.filter(!_.getMarkId().equals(mark.getMarkId()))
        if (null == newMarks || newMarks.isEmpty) {
          getMarkReqAndMarkCache().remove(mark.getMarkReq)
        } else {
          getMarkReqAndMarkCache().put(mark.getMarkReq, newMarks)
        }
        //getMarkReqAndMarkCache().put(mark.getMarkReq))
      }
    }
  }

  protected def getAllInstances(): Array[String] = MARK_CACHE_LOCKER.synchronized {
    val instances = new ArrayBuffer[String]
    getMarkCache().values().foreach(_.foreach(s => instances.add(s.getInstance)))
    instances.toArray
  }

  override def getPolicy(): Policy = Policy.Label

}
