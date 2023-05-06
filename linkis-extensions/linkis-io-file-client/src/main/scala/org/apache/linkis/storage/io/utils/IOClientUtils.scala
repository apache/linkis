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

package org.apache.linkis.storage.io.utils

import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{
  CodeLanguageLabel,
  ConcurrentEngineConnLabel,
  RunType,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.entity.entrance.LoadBalanceLabel
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import org.apache.linkis.storage.io.conf.IOFileClientConf
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}
import org.apache.linkis.storage.utils.StorageConfiguration.IO_USER

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._

object IOClientUtils {

  val SUCCESS = "SUCCESS"
  val FAILED = "FAILED"
  private val idGenerator = new AtomicInteger(0)

  private val jobGroupIDGenerator = new AtomicInteger(0)

  private lazy val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  private val loadBalanceLabel = {
    val label =
      labelBuilderFactory.createLabel[LoadBalanceLabel](LabelKeyConstant.LOAD_BALANCE_KEY)
    label.setCapacity(IOFileClientConf.IO_LOADBALANCE_CAPACITY.getValue)
    label.setGroupId("ioclient")
    label
  }

  private val codeTypeLabel = {
    val label = labelBuilderFactory.createLabel[CodeLanguageLabel](LabelKeyConstant.CODE_TYPE_KEY)
    label.setCodeType(RunType.IO_FILE.toString)
    label
  }

  private val conCurrentLabel = {
    val label = labelBuilderFactory.createLabel(classOf[ConcurrentEngineConnLabel])
    label.setParallelism("10")
    label
  }

  private val creator = StorageConfiguration.IO_DEFAULT_CREATOR.getValue

  def generateExecID(): String = {
    "io_" + idGenerator.getAndIncrement() + System.currentTimeMillis()
  }

  def generateJobGrupID(): String = {
    "io_jobGrup_" + jobGroupIDGenerator.getAndIncrement()
  }

  def getLabelBuilderFactory: LabelBuilderFactory = labelBuilderFactory

  def getDefaultLoadBalanceLabel: LoadBalanceLabel = {
    loadBalanceLabel
  }

  def getExtraLabels(): Array[Label[_]] = {
    val labelJson = IOFileClientConf.IO_EXTRA_LABELS.getValue
    if (StringUtils.isNotBlank(labelJson)) {
      val labelMap =
        BDPJettyServerHelper.gson.fromJson(labelJson, classOf[java.util.Map[String, AnyRef]])
      labelBuilderFactory.getLabels(labelMap).asScala.toArray
    } else {
      Array.empty[Label[_]]
    }
  }

  def addLabelToParams(label: Label[_], params: util.Map[String, AnyRef]): Unit = {
    val labelMap = TaskUtils.getLabelsMap(params)
    labelMap.put(label.getLabelKey, label.getStringValue)
    TaskUtils.addLabelsMap(params, labelMap)
  }

  def buildJobReq(
      user: String,
      methodEntity: MethodEntity,
      params: java.util.Map[String, AnyRef]
  ): JobReq = {

    val labelMap = TaskUtils.getLabelsMap(params)
    val labels: util.List[Label[_]] =
      labelBuilderFactory.getLabels(labelMap)
    labels.add(codeTypeLabel)
    labels.add(conCurrentLabel)

    val rootUser =
      if (methodEntity.getFsType.equals(StorageUtils.HDFS))
        StorageConfiguration.HDFS_ROOT_USER.getValue
      else StorageConfiguration.LOCAL_ROOT_USER.getValue
    val userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
    userCreatorLabel.setCreator(creator)
    userCreatorLabel.setUser(rootUser)
    labels.add(userCreatorLabel)

    val code = MethodEntitySerializer.serializer(methodEntity)
    val codes = new util.ArrayList[String]()
    codes.add(code)
    val codeLogicalUnit = new CodeLogicalUnit(codes, codeTypeLabel)

    val jobReqBuilder = ComputationJobReq.newBuilder()
    jobReqBuilder.setId(generateExecID())
    jobReqBuilder.setSubmitUser(user)
    jobReqBuilder.setExecuteUser(IO_USER.getValue)
    jobReqBuilder.setCodeLogicalUnit(codeLogicalUnit)
    jobReqBuilder.setLabels(labels)
    jobReqBuilder.setParams(params)
    jobReqBuilder.build()
  }

}
