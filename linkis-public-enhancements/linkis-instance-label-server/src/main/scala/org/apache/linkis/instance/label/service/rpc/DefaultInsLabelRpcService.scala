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

package org.apache.linkis.instance.label.service.rpc

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.instance.label.dao.InsLabelRelationDao
import org.apache.linkis.instance.label.service.{InsLabelRpcService, InsLabelServiceAdapter}
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.LabelUtils
import org.apache.linkis.protocol.label._
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.{PostConstruct, Resource}

import java.util

import scala.collection.JavaConverters._
import scala.collection.JavaConverters.asScalaBufferConverter

@Service
class DefaultInsLabelRpcService extends InsLabelRpcService with Logging {

  @Resource
  private var insLabelService: InsLabelServiceAdapter = _

  @Autowired
  private var inslabelRelationDao: InsLabelRelationDao = _

  private val labelBuilderFactory: LabelBuilderFactory =
    LabelBuilderFactoryContext.getLabelBuilderFactory

  @PostConstruct
  def init(): Unit = {
    logger.info("Use the default implement of rpc service: DefaultInsLabelRpcService")
  }

  @Receiver
  override def attachLabelsToInstance(
      sender: Sender,
      insLabelAttachRequest: InsLabelAttachRequest
  ): Unit = {
    val labelMap = Option(insLabelAttachRequest.getLabels)
    val instance = Option(insLabelAttachRequest.getServiceInstance).getOrElse(
      throw new ErrorException(-1, "field 'serviceInstance' in attachRequest cannot be blank")
    )
    val labels = getLabels(labelMap).asScala.filter(_ != null)
    logger.info(s"Start to attach labels[$labels] to instance[$instance]")
    insLabelService.attachLabelsToInstance(labels.asJava, instance)
    logger.info(s"Success to attach labels[$labels] to instance[$instance]")
  }

  @Receiver
  override def refreshLabelsToInstance(
      sender: Sender,
      insLabelRefreshRequest: InsLabelRefreshRequest
  ): Unit = {
    val labelMap = Option(insLabelRefreshRequest.getLabels)
    val instance = Option(insLabelRefreshRequest.getServiceInstance).getOrElse(
      throw new ErrorException(-1, "field 'serviceInstance' in refreshRequest cannot be blank")
    )
    val labels = getLabels(labelMap)
    logger.info(s"Start to refresh labels[$labels] to instance[$instance]")
    insLabelService.refreshLabelsToInstance(labels, instance)
    logger.info(s"Success to refresh labels[$labels] to instance[$instance]")
  }

  private def getLabels(labelMap: Option[util.Map[String, Object]]): util.List[Label[_]] = {
    if (labelMap.isDefined) {
      LabelBuilderFactoryContext.getLabelBuilderFactory.getLabels(labelMap.get)
    } else {
      new util.ArrayList[Label[_]]
    }
  }

  @Receiver
  override def removeLabelsFromInstance(
      sender: Sender,
      insLabelRemoveRequest: InsLabelRemoveRequest
  ): Unit = {
    val instance = Option(insLabelRemoveRequest.getServiceInstance).getOrElse(
      throw new ErrorException(-1, "field 'serviceInstance' in removeRequest cannot be blank")
    )
    logger.info(s"Start to remove labels from instance[$instance]")
    insLabelService.removeLabelsFromInstance(instance)
    insLabelService.removeInstance(instance)
    logger.info(s"Success to remove labels from instance[$instance]")
    logger.info(s"Success to remove instance[$instance]")
  }

  @Receiver
  override def queryLabelsFromInstance(
      sender: Sender,
      insLabelQueryRequest: InsLabelQueryRequest
  ): InsLabelQueryResponse = {
    Utils.tryAndError {
      val labels = new util.ArrayList[Label[_]]()
      inslabelRelationDao
        .searchLabelsByInstance(insLabelQueryRequest.getServiceInstance.getInstance)
        .asScala
        .map(insLabel =>
          labelBuilderFactory.createLabel[Label[_]](insLabel.getLabelKey, insLabel.getStringValue)
        )
        .foreach(l => labels.add(l))
      new InsLabelQueryResponse(LabelUtils.labelsToPairList(labels))
    }
  }

  @Receiver
  override def queryInstanceFromLabels(
      sender: Sender,
      labelInsQueryRequest: LabelInsQueryRequest
  ): LabelInsQueryResponse = {
    Utils.tryAndError {
      val labels =
        LabelBuilderFactoryContext.getLabelBuilderFactory.getLabels(labelInsQueryRequest.getLabels)
      new LabelInsQueryResponse(insLabelService.searchInstancesByLabels(labels))
    }
  }

}
