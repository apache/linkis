/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.ecm

import java.util
import java.util.Random

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.engine.ReuseExclusionLabel
import com.webank.wedatasphere.linkis.manager.label.entity.entrance.{BindEngineLabel, LoadBalanceLabel}
import com.webank.wedatasphere.linkis.orchestrator.ecm.conf.ECMPluginConf
import com.webank.wedatasphere.linkis.orchestrator.ecm.entity._
import com.webank.wedatasphere.linkis.orchestrator.ecm.exception.ECMPluginErrorException
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineConnExecutor
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.collections.CollectionUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class LoadBalanceLabelEngineConnManager extends ComputationEngineConnManager with Logging {


  private val markReqAndMarkCache = new util.HashMap[MarkReq, util.List[Mark]]()
  private val idToMarkCache = new util.HashMap[String, Mark]()

  private def getMarkReqAndMarkCache(): util.Map[MarkReq, util.List[Mark]] = markReqAndMarkCache

  private def getIdToMarkCache(): util.Map[String, Mark] = idToMarkCache

  private val MARK_REQ_CACHE_LOCKER = new Object()


  override def applyMark(markReq: MarkReq): Mark = {
    if (null == markReq) return null
    val markReqCache = MARK_REQ_CACHE_LOCKER.synchronized {
      getMarkReqAndMarkCache().keys
    }
    var mayBeMarkReq = markReqCache.find(_.equals(markReq)).orNull
    val markNum: Int = {
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
    var count = 0
    if (null != mayBeMarkReq && !getMarkReqAndMarkCache().get(mayBeMarkReq).isEmpty) {
      count = getMarkReqAndMarkCache().get(mayBeMarkReq).size()
    }

    // toto check if count >= markNum, will not add more mark
    while (count < markNum) {
      count += 1
      createMark(markReq)
    }
    if (null == mayBeMarkReq) {
      val markReqCache2 = MARK_REQ_CACHE_LOCKER.synchronized {
        getMarkReqAndMarkCache().keys
      }
      mayBeMarkReq = markReqCache2.find(_.equals(markReq)).get
    }
    // markReq is in cache, and mark list is ready
    val markList = getMarkReqAndMarkCache().get(mayBeMarkReq)
    var choosedMark: Mark = null
    if (mayBeMarkReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
      val bindEngineLabel = MarkReq.getLabelBuilderFactory.createLabel[BindEngineLabel](LabelKeyConstant.BIND_ENGINE_KEY,
        mayBeMarkReq.getLabels.get(LabelKeyConstant.BIND_ENGINE_KEY))
      if (bindEngineLabel.getIsJobGroupHead) {
        // getRandom mark
        choosedMark = markList.get(new util.Random().nextInt(markList.length))
        choosedMark.asInstanceOf[LoadBalanceMark].setTaskMarkReq(markReq)
        getIdToMarkCache().put(bindEngineLabel.getJobGroupId, choosedMark)
      } else {
        // get mark from cache
        if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
          choosedMark = getIdToMarkCache().get(bindEngineLabel.getJobGroupId)
          choosedMark.asInstanceOf[LoadBalanceMark].setTaskMarkReq(markReq)
          val insList = getMarkCache().get(choosedMark)
          if (null == insList || insList.size() != 1) {
            val msg = s"Engine instance releated to choosedMark : ${BDPJettyServerHelper.gson.toJson(choosedMark)} with bindEngineLabel : ${bindEngineLabel.getStringValue} cannot be null"
            error(msg)
            throw new ECMPluginErrorException(ECMPluginConf.ECM_CACHE_ERROR_CODE, msg)
          }
        } else {
          val msg = s"Cannot find mark related to bindEngineLabel : ${bindEngineLabel.getStringValue}"
          error(msg)
          throw new ECMPluginErrorException(ECMPluginConf.ECM_CACHE_ERROR_CODE, msg)
        }
      }
      if (bindEngineLabel.getIsJobGroupEnd) {
        if (getIdToMarkCache().containsKey(bindEngineLabel.getJobGroupId)) {
          getIdToMarkCache().remove(bindEngineLabel.getJobGroupId)
        } else {
          error(s"Cannot find mark related to bindEngineLabel : ${bindEngineLabel.getStringValue}, cannot remove it.")
        }
      }
    } else {
      // treat as isHead and isEnd
      choosedMark = markList.get(new Random().nextInt(count))
    }
    choosedMark
  }

  /**
    * 1. Create a new Mark
    * 2. There will be a process of requesting the engine when generating a new Mark. If the request is received, it will be stored in the Map: Mark is Key, EngineConnExecutor is Value
    * 3. The number of Marks generated is equal to the concurrent amount of LoadBalance
    */
  override def createMark(markReq: MarkReq): Mark = {
    val mark = new LoadBalanceMark(nextMarkId(), markReq)
    addMark(mark, new util.ArrayList[ServiceInstance]())
    addMarkReqAndMark(markReq, mark)
    mark
  }

  private def addMarkReqAndMark(req: MarkReq, mark: DefaultMark): Unit = {
    if (null != req) {
      val markReqCache = MARK_REQ_CACHE_LOCKER.synchronized {
        getMarkReqAndMarkCache().keys
      }
      val maybeMarkReq = markReqCache.find(_.eq(req)).getOrElse(null)
      if (null != maybeMarkReq) {
        val markList = markReqAndMarkCache.get(maybeMarkReq)
        val mayBeMark = markList.find(_.getMarkId().equals(mark.getMarkId())).getOrElse(null)
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
    info(s"mark ${mark.getMarkId()} start to tryReuseEngineConnExecutor")
    if (null != mark && getMarkCache().containsKey(mark)) {
      tryReuseEngineConnExecutor(mark) match {
        case Some(engineConnExecutor) => return engineConnExecutor
        case None =>
      }
    } else {
      throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"mark cannot be null")
    }
    info(s"Start to askEngineConnExecutor for marks in MarkCache")
    getMarkCache().keySet().foreach{ cachedMark =>
      val serviceInstances = getMarkCache().get(cachedMark)
      if(CollectionUtils.isEmpty(serviceInstances)){
        info(s"mark ${cachedMark.getMarkId()} start to askEngineConnExecutor")
        val engineConnAskReq = cachedMark.getMarkReq.createEngineConnAskReq()
        val reuseExclusionLabel = MarkReq.getLabelBuilderFactory.createLabel(classOf[ReuseExclusionLabel])
        reuseExclusionLabel.setInstances(getAllInstances())
        engineConnAskReq.getLabels.put(LabelKeyConstant.REUSE_EXCLUSION_KEY, reuseExclusionLabel.getValue)
        val engineConnExecutor = askEngineConnExecutor(engineConnAskReq)
        saveToMarkCache(cachedMark, engineConnExecutor)
        info(s"mark ${cachedMark.getMarkId()} Finished to  getAvailableEngineConnExecutor by create")
      }
    }
    tryReuseEngineConnExecutor(mark) match {
      case Some(engineConnExecutor) =>
        engineConnExecutor.useEngineConn
        return engineConnExecutor
      case None =>
        throw new ECMPluginErrorException(ECMPluginConf.ECM_ERROR_CODE, s"mark ${mark.getMarkId()} failed to start engineConn")
    }
  }

  override protected def tryReuseEngineConnExecutor(mark: Mark):Option[EngineConnExecutor] = {
    val instances = getInstances(mark)
    if (null == instances || instances.isEmpty) {
      return None
    }
    val taskMarkReq = mark.asInstanceOf[LoadBalanceMark].getTaskMarkReq()
    val bindEngineLabel = {
      if (taskMarkReq.getLabels.containsKey(LabelKeyConstant.BIND_ENGINE_KEY)) {
        MarkReq.getLabelBuilderFactory.createLabel[BindEngineLabel](LabelKeyConstant.BIND_ENGINE_KEY,
          taskMarkReq.getLabels.get(LabelKeyConstant.BIND_ENGINE_KEY))
      } else {
        null
      }
    }
    if (null != bindEngineLabel) {
      val executor = getEngineConnExecutorCache().get(instances.head)
      if (bindEngineLabel.getIsJobGroupHead) {
        if (null != executor) {
          return Some(executor)
        } else {
          return None
        }
      } else {
        if (null != executor) {
          return Some(executor)
        } else {
          val msg = s"Instance : ${instances.head.getInstance} cannot found in cache, but with bindEngineLabel : ${bindEngineLabel.getStringValue}"
          error(msg)
          throw new ECMPluginErrorException(ECMPluginConf.ECM_ENGINE_CACHE_ERROR, msg)
        }
      }
    } else {
      val executor = getEngineConnExecutorCache().get(instances.head)
      if (null != executor) {
        return Some(executor)
      } else {
        return None
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
