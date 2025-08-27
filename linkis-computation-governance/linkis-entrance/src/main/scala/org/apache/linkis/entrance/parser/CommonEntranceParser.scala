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

package org.apache.linkis.entrance.parser

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceIllegalParamException}
import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.entrance.timeout.JobTimeoutManager
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.common.conf.RMConfiguration
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.queue.SchedulerEventState

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Date

import scala.collection.JavaConverters._

class CommonEntranceParser(val persistenceManager: PersistenceManager)
    extends AbstractEntranceParser
    with Logging {

  private val labelBuilderFactory: LabelBuilderFactory =
    LabelBuilderFactoryContext.getLabelBuilderFactory

  override protected def getPersistenceManager(): PersistenceManager = persistenceManager

  /**
   * parse params to be a task params json data from frontend
   */
  override def parseToTask(params: util.Map[String, AnyRef]): JobRequest = {
    if (!params.containsKey(TaskConstant.EXECUTION_CONTENT)) {
      return parseToOldTask(params)
    }

    // 1. set User
    val jobRequest = new JobRequest
    jobRequest.setCreatedTime(new Date(System.currentTimeMillis))
    val executeUser = params.get(TaskConstant.EXECUTE_USER).asInstanceOf[String]
    val submitUser = params.get(TaskConstant.SUBMIT_USER).asInstanceOf[String]
    jobRequest.setSubmitUser(submitUser)
    if (StringUtils.isBlank(executeUser)) {
      jobRequest.setExecuteUser(submitUser)
    } else {
      jobRequest.setExecuteUser(executeUser)
    }
    // 2. parse params
    val executionContent = params
      .getOrDefault(TaskConstant.EXECUTION_CONTENT, new util.HashMap[String, String]())
      .asInstanceOf[util.Map[String, AnyRef]]
    val configMap = params
      .getOrDefault(TaskConstant.PARAMS, new util.HashMap[String, AnyRef]())
      .asInstanceOf[util.Map[String, AnyRef]]
    val labelMap = params
      .getOrDefault(TaskConstant.LABELS, new util.HashMap[String, AnyRef]())
      .asInstanceOf[util.Map[String, AnyRef]]
    val source = params
      .getOrDefault(TaskConstant.SOURCE, new util.HashMap[String, String]())
      .asInstanceOf[util.Map[String, String]]
    if (labelMap.isEmpty) {
      throw new EntranceIllegalParamException(
        EntranceErrorCode.PARAM_CANNOT_EMPTY.getErrCode,
        s"${EntranceErrorCode.PARAM_CANNOT_EMPTY.getDesc},  labels is null"
      )
    }
    // 3. set Code
    var code: String = null
    var runType: String = null
    if (executionContent.containsKey(TaskConstant.CODE)) {
      code = executionContent.get(TaskConstant.CODE).asInstanceOf[String]
      runType = executionContent.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isBlank(code)) {
        throw new EntranceIllegalParamException(
          PARAM_NOT_NULL.getErrorCode,
          PARAM_NOT_NULL.getErrorDesc
        )
      }
    } else {
      // todo check
      throw new EntranceIllegalParamException(
        ONLY_CODE_SUPPORTED.getErrorCode,
        PARAM_NOT_NULL.getErrorDesc
      )
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    if (formatCode) code = format(code)
    jobRequest.setExecutionCode(code)
    // 4. parse label
    val labels: util.Map[String, Label[_]] = buildLabel(labelMap)
    JobTimeoutManager.checkTimeoutLabel(labels)
    checkEngineTypeLabel(labels)
    generateAndVerifyCodeLanguageLabel(runType, labels)
    generateAndVerifyUserCreatorLabel(executeUser, labels)
    generateAndVerifyClusterLabel(labels)

    jobRequest.setLabels(new util.ArrayList[Label[_]](labels.values()))
    jobRequest.setSource(source)
    jobRequest.setStatus(SchedulerEventState.Inited.toString)
    // Entry indicator: task submission time
    jobRequest.setMetrics(new util.HashMap[String, AnyRef]())
    jobRequest.getMetrics.put(TaskConstant.JOB_SUBMIT_TIME, new Date(System.currentTimeMillis))
    jobRequest.setParams(configMap)
    jobRequest
  }

  private def checkEngineTypeLabel(labels: util.Map[String, Label[_]]): Unit = {
    val engineTypeLabel = labels.getOrDefault(LabelKeyConstant.ENGINE_TYPE_KEY, null)
    if (null == engineTypeLabel) {
      val msg = s"You need to specify engineTypeLabel in labels," +
        s"such as spark-${LabelCommonConfig.SPARK_ENGINE_VERSION.getValue}"
      throw new EntranceIllegalParamException(
        EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg
      )
    }
  }

  /**
   * Generate and verify CodeLanguageLabel
   *
   * @param runType
   * @param labels
   */
  private def generateAndVerifyCodeLanguageLabel(
      runType: String,
      labels: util.Map[String, Label[_]]
  ): Unit = {
    val engineRunTypeLabel = labels.getOrDefault(LabelKeyConstant.CODE_TYPE_KEY, null)
    if (StringUtils.isBlank(runType) && null == engineRunTypeLabel) {
      val msg = "You need to specify runType in execution content, such as sql"
      logger.warn(msg)
      throw new EntranceIllegalParamException(
        EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg
      )
    } else if (StringUtils.isNotBlank(runType)) {
      val codeLanguageLabel =
        labelBuilderFactory.createLabel[CodeLanguageLabel](LabelKeyConstant.CODE_TYPE_KEY)
      codeLanguageLabel.setCodeType(runType)
      labels.put(LabelKeyConstant.CODE_TYPE_KEY, codeLanguageLabel)
    }
  }

  /**
   * Generate and verify UserCreatorLabel
   *
   * @param executeUser
   * @param labels
   */
  private def generateAndVerifyUserCreatorLabel(
      executeUser: String,
      labels: util.Map[String, Label[_]]
  ): Unit = {
    var userCreatorLabel = labels
      .getOrDefault(LabelKeyConstant.USER_CREATOR_TYPE_KEY, null)
      .asInstanceOf[UserCreatorLabel]
    if (null == userCreatorLabel) {
      userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
      val creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getHotValue()
      userCreatorLabel.setUser(executeUser)
      userCreatorLabel.setCreator(creator)
      labels.put(userCreatorLabel.getLabelKey, userCreatorLabel)
    }
  }

  private def generateAndVerifyClusterLabel(labels: util.Map[String, Label[_]]): Unit = {
    if (!Configuration.IS_MULTIPLE_YARN_CLUSTER) {
      return
    }
    var clusterLabel = labels
      .getOrDefault(LabelKeyConstant.YARN_CLUSTER_KEY, null)
      .asInstanceOf[ClusterLabel]
    if (clusterLabel == null) {
      clusterLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[ClusterLabel])
      clusterLabel.setClusterName(RMConfiguration.DEFAULT_YARN_CLUSTER_NAME.getValue)
      clusterLabel.setClusterType(RMConfiguration.DEFAULT_YARN_TYPE.getValue)
      labels.put(clusterLabel.getLabelKey, clusterLabel)
    }
  }

  private def parseToOldTask(params: util.Map[String, AnyRef]): JobRequest = {

    val jobReq = new JobRequest
    jobReq.setCreatedTime(new Date(System.currentTimeMillis))
    val umUser = params.get(TaskConstant.EXECUTE_USER).asInstanceOf[String]
    val submitUser = params.get(TaskConstant.SUBMIT_USER).asInstanceOf[String]
    jobReq.setSubmitUser(submitUser)
    if (StringUtils.isBlank(submitUser)) {
      jobReq.setSubmitUser(umUser)
    }
    if (umUser == null) {
      throw new EntranceIllegalParamException(
        EXECUTEUSER_NOT_NULL.getErrorCode,
        EXECUTEUSER_NOT_NULL.getErrorDesc
      )
    }
    jobReq.setExecuteUser(umUser)
    var executionCode = params.get(TaskConstant.EXECUTIONCODE).asInstanceOf[String]
    val _params = params.get(TaskConstant.PARAMS)
    _params match {
      case mapParams: java.util.Map[String, AnyRef] => jobReq.setParams(mapParams)
      case _ =>
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    var creator = params.get(TaskConstant.REQUESTAPPLICATIONNAME).asInstanceOf[String]
    val source = params
      .getOrDefault(TaskConstant.SOURCE, new util.HashMap[String, String]())
      .asInstanceOf[util.Map[String, String]]
    val executeApplicationName =
      params.get(TaskConstant.EXECUTEAPPLICATIONNAME).asInstanceOf[String]
    if (StringUtils.isBlank(creator)) {
      creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getHotValue()
    }
    // When the execution type is IDE, executioncode and scriptpath cannot be empty at the same time
    if (
        EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME
          .getHotValue()
          .equals(creator) && StringUtils.isEmpty(source.get(TaskConstant.SCRIPTPATH)) &&
        StringUtils.isEmpty(executionCode)
    ) {
      throw new EntranceIllegalParamException(
        EXEC_SCRIP_NOT_NULL.getErrorCode,
        EXEC_SCRIP_NOT_NULL.getErrorDesc
      )
    }
    var runType: String = null
    if (StringUtils.isNotEmpty(executionCode)) {
      runType = params.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isEmpty(runType)) {
        runType = EntranceConfiguration.DEFAULT_RUN_TYPE.getHotValue()
      }
      // If formatCode is not empty, we need to format it(如果formatCode 不为空的话，我们需要将其进行格式化)
      if (formatCode) executionCode = format(executionCode)
      jobReq.setExecutionCode(executionCode)
    }
    val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(executeApplicationName)
    val runTypeLabel =
      labelBuilderFactory.createLabel[Label[_]](LabelKeyConstant.CODE_TYPE_KEY, runType)
    val userCreatorLabel = labelBuilderFactory
      .createLabel[Label[_]](LabelKeyConstant.USER_CREATOR_TYPE_KEY, umUser + "-" + creator)

    val labelList = new util.ArrayList[Label[_]](3)
    labelList.add(engineTypeLabel)
    labelList.add(runTypeLabel)
    labelList.add(userCreatorLabel)
    if (jobReq.getParams != null) {
      val labelMap = params
        .getOrDefault(TaskConstant.LABELS, new util.HashMap[String, AnyRef]())
        .asInstanceOf[util.Map[String, AnyRef]]
      if (null != labelMap && !labelMap.isEmpty) {
        val list: util.List[Label[_]] =
          labelBuilderFactory.getLabels(labelMap)
        labelList.addAll(list)
      }
    }
    jobReq.setProgress("0.0")
    jobReq.setSource(source)
    // In order to be compatible with the code, let enginetype and runtype have the same attribute
    jobReq.setStatus(SchedulerEventState.Inited.toString)
    // Package labels
    jobReq.setLabels(labelList)
    jobReq.setMetrics(new util.HashMap[String, AnyRef]())
    jobReq.getMetrics.put(TaskConstant.JOB_SUBMIT_TIME, new Date(System.currentTimeMillis))
    jobReq
  }

  private def buildLabel(labelMap: util.Map[String, AnyRef]): util.Map[String, Label[_]] = {
    val labelKeyValueMap = new util.HashMap[String, Label[_]]()
    if (null != labelMap && !labelMap.isEmpty) {
      val list: util.List[Label[_]] =
        labelBuilderFactory.getLabels(labelMap)
      if (null != list) {
        list.asScala.filter(_ != null).foreach { label =>
          labelKeyValueMap.put(label.getLabelKey, label)
        }
      }
    }
    labelKeyValueMap
  }

  // todo to format code using proper way
  private def format(code: String): String = code

}
