/*
 *
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
package com.webank.wedatasphere.linkis.manager.am.service.engine

import java.util
import java.util.concurrent.{TimeUnit, TimeoutException}

import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration
import com.webank.wedatasphere.linkis.manager.am.label.EngineReuseLabelChooser
import com.webank.wedatasphere.linkis.manager.am.selector.NodeSelector
import com.webank.wedatasphere.linkis.manager.am.utils.AMUtils
import com.webank.wedatasphere.linkis.manager.common.constant.AMConstant
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineReuseRequest
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.{EngineNodeLabel, Label}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.ReuseExclusionLabel
import com.webank.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.{NodeLabelService, UserLabelService}
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

@Service
class DefaultEngineReuseService extends AbstractEngineService with EngineReuseService with Logging {

  @Autowired
  private var nodeSelector: NodeSelector = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var userLabelService: UserLabelService = _

  @Autowired
  private var engineReuseLabelChoosers: util.List[EngineReuseLabelChooser] = _

  @Receiver
  @throws[LinkisRetryException]
  override def reuseEngine(engineReuseRequest: EngineReuseRequest): EngineNode = {
    info(s"Start to reuse Engine for request: $engineReuseRequest")
    //TODO Label Factory And builder
    val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
    //Label:传入的Label和用户默认的Label 去重
    var labelList: util.List[Label[_]] = LabelUtils.distinctLabel(labelBuilderFactory.getLabels(engineReuseRequest.getLabels),
      userLabelService.getUserLabels(engineReuseRequest.getUser))

    labelList = labelList.filter(_.isInstanceOf[EngineNodeLabel])

    val engineConnAliasLabel = labelBuilderFactory.createLabel(classOf[AliasServiceInstanceLabel])
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue)
    labelList.add(engineConnAliasLabel)

    //label chooser
    if (null != engineReuseLabelChoosers) {
      engineReuseLabelChoosers.foreach { chooser =>
        labelList = chooser.chooseLabels(labelList)
      }
    }

    val instances = nodeLabelService.getScoredNodeMapsByLabels(labelList)
    if (null == instances || instances.isEmpty) {
      throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"No engine can be reused")
    }
    labelList.find(_.isInstanceOf[ReuseExclusionLabel]) match {
      case Some(l) =>
        val exclusionInstances = l.asInstanceOf[ReuseExclusionLabel].getInstances
        val instancesIterator = instances.iterator
        while(instancesIterator.hasNext){
          val instance = instancesIterator.next
          if(exclusionInstances.contains(instance._1.getServiceInstance.getInstance)){
            instancesIterator.remove
          }
        }
      case None =>
    }
    if (null == instances || instances.isEmpty) {
      throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"No engine can be reused")
    }
    var engineScoreList = getEngineNodeManager.getEngineNodes(instances.map(_._1).toSeq.toArray)

    var engine: EngineNode = null
    var count = 1
    val timeout = if (engineReuseRequest.getTimeOut <= 0) AMConfiguration.ENGINE_REUSE_MAX_TIME.getValue.toLong else engineReuseRequest.getTimeOut
    val reuseLimit = if (engineReuseRequest.getReuseCount <= 0) AMConfiguration.ENGINE_REUSE_COUNT_LIMIT.getValue else engineReuseRequest.getReuseCount

    def selectEngineToReuse: Boolean = {
      if (count > reuseLimit) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Engine reuse exceeds limit: $reuseLimit")
      }
      //3. 执行Select 判断label分数、判断是否可用、判断负载
      val choseNode = nodeSelector.choseNode(engineScoreList.toArray)
      //4. 获取Select后排在第一的engine，修改EngineNode的Label为新标签，并调用EngineNodeManager的reuse请求
      if (choseNode.isEmpty) {
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, "No engine can be reused")
      }
      //TODO 需要加上Label不匹配判断？如果
      //5. 调用EngineNodeManager 进行reuse 如果reuse失败，则去掉该engine进行重新reuse走3和4
      engine = Utils.tryAndWarn(getEngineNodeManager.reuseEngine(choseNode.get.asInstanceOf[EngineNode]))
      if (null == engine) {
        count = count + 1
        engineScoreList = engineScoreList.filter(!_.equals(choseNode.get))
      }
      null != engine
    }

    val startTime = System.currentTimeMillis()
    try {
      Utils.waitUntil(() => selectEngineToReuse, Duration(timeout, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException =>
        throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, s"Waiting for Engine initialization failure, already waiting $timeout ms")
      case t: Throwable =>
        info(s"Failed to reuse engineConn time taken ${System.currentTimeMillis() - startTime}")
        throw t
    }
    info(s"Finished to reuse Engine for request: $engineReuseRequest get EngineNode $engine, time taken ${System.currentTimeMillis() - startTime}")
    val engineServiceLabelList = instances.filter(kv => kv._1.getServiceInstance.equals(engine.getServiceInstance))
    if (null != engineServiceLabelList && engineServiceLabelList.nonEmpty) {
      engine.setLabels(engineServiceLabelList.head._2)
    } else {
      error("Get choosen engineNode : " + AMUtils.GSON.toJson(engine) + " from engineLabelMap : " + AMUtils.GSON.toJson(instances))
    }
    engine
  }




}
