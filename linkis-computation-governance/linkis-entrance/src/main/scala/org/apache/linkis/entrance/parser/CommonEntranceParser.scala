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
 
package org.apache.linkis.entrance.parser

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceIllegalParamException}
import org.apache.linkis.entrance.persistence.PersistenceManager
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.builder.factory.{LabelBuilderFactory, LabelBuilderFactoryContext, StdLabelBuilderFactory}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.queue.SchedulerEventState
import org.apache.commons.lang.StringUtils
import java.util
import java.util.Date

import org.apache.linkis.entrance.timeout.JobTimeoutManager

import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConverters._


class CommonEntranceParser(val persistenceManager: PersistenceManager) extends AbstractEntranceParser with Logging {

  private val labelBuilderFactory: LabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override protected def getPersistenceManager(): PersistenceManager = persistenceManager

  /**
    * parse params to be a task
    * params json data from frontend
    *
    */
  override def parseToTask(params: util.Map[String, Any]): JobRequest = {
    if (!params.containsKey(TaskConstant.EXECUTION_CONTENT)) {
      return parseToOldTask(params)
    }

    //1. set User
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
    //2. parse params
    val executionContent = params.getOrDefault(TaskConstant.EXECUTION_CONTENT, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, Object]]
    val configMap = params.getOrDefault(TaskConstant.PARAMS, new util.HashMap[String, Object]()).asInstanceOf[util.Map[String, Object]]
    val labelMap = params.getOrDefault(TaskConstant.LABELS, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, Object]]
    val source = params.getOrDefault(TaskConstant.SOURCE, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, String]]
    if (labelMap.isEmpty) {
      throw new EntranceIllegalParamException(EntranceErrorCode.PARAM_CANNOT_EMPTY.getErrCode, EntranceErrorCode.PARAM_CANNOT_EMPTY.getDesc + s",  labels is null")
    }
    //3. set Code
    var code: String = null
    var runType: String = null
    if (executionContent.containsKey(TaskConstant.CODE)) {
      code = executionContent.get(TaskConstant.CODE).asInstanceOf[String]
      runType = executionContent.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isEmpty(code))
        throw new EntranceIllegalParamException(20007, "param executionCode can not be empty ")
    } else {
      // todo check
      throw new EntranceIllegalParamException(20010, "Only code with runtype supported !")
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    if (formatCode) code = format(code)
    jobRequest.setExecutionCode(code)
    //4. parse label
    val labels: util.Map[String, Label[_]] = buildLabel(labelMap)
    JobTimeoutManager.checkTimeoutLabel(labels)
    checkEngineTypeLabel(labels)
    generateAndVerifyCodeLanguageLabel(runType, labels)
    generateAndVerifyUserCreatorLabel(executeUser, labels)

    jobRequest.setLabels(new util.ArrayList[Label[_]](labels.values()))
    jobRequest.setSource(source)
    jobRequest.setStatus(SchedulerEventState.Inited.toString)
    //Entrance指标：任务提交时间
    jobRequest.setMetrics(new util.HashMap[String, Object]())
    jobRequest.getMetrics.put(TaskConstant.ENTRANCEJOB_SUBMIT_TIME, new Date(System.currentTimeMillis))
    jobRequest.setParams(configMap)
    jobRequest
  }

  private def checkEngineTypeLabel(labels: util.Map[String, Label[_]]): Unit = {
    val engineTypeLabel = labels.getOrElse(LabelKeyConstant.ENGINE_TYPE_KEY, null)
    if (null == engineTypeLabel) {
      val msg = s"You need to specify engineTypeLabel in labels, such as spark-2.4.3"
      throw new EntranceIllegalParamException(EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode, EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg)
    }
  }

  /**
    * Generate and verify CodeLanguageLabel
    *
    * @param runType
    * @param labels
    */
  private def generateAndVerifyCodeLanguageLabel(runType: String, labels: util.Map[String, Label[_]]): Unit = {
    val engineRunTypeLabel = labels.getOrElse(LabelKeyConstant.CODE_TYPE_KEY, null)
    if (StringUtils.isBlank(runType) && null == engineRunTypeLabel) {
      val msg = s"You need to specify runType in execution content, such as sql"
      warn(msg)
      throw new EntranceIllegalParamException(EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg)
    } else if (StringUtils.isNotBlank(runType)) {
      val codeLanguageLabel = labelBuilderFactory.createLabel[CodeLanguageLabel](LabelKeyConstant.CODE_TYPE_KEY)
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
  private def generateAndVerifyUserCreatorLabel(executeUser: String, labels: util.Map[String, Label[_]]): Unit = {
    var userCreatorLabel = labels.getOrElse(LabelKeyConstant.USER_CREATOR_TYPE_KEY, null).asInstanceOf[UserCreatorLabel]
    if (null == userCreatorLabel) {
      userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
      val creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue
      userCreatorLabel.setUser(executeUser)
      userCreatorLabel.setCreator(creator)
      labels.put(userCreatorLabel.getLabelKey, userCreatorLabel)
    }
  }

  private def parseToOldTask(params: util.Map[String, Any]): JobRequest = {

    val jobReq = new JobRequest
    jobReq.setCreatedTime(new Date(System.currentTimeMillis))
    val umUser = params.get(TaskConstant.UMUSER).asInstanceOf[String]
    val submitUser = params.get(TaskConstant.SUBMIT_USER).asInstanceOf[String]
    jobReq.setSubmitUser(submitUser)
    if (StringUtils.isBlank(submitUser)) {
      jobReq.setSubmitUser(umUser)
    }
    if (umUser == null) throw new EntranceIllegalParamException(20005, "umUser can not be null")
    jobReq.setExecuteUser(umUser)
    var executionCode = params.get(TaskConstant.EXECUTIONCODE).asInstanceOf[String]
    val _params = params.get(TaskConstant.PARAMS)
    _params match {
      case mapParams: java.util.Map[String, Object] => jobReq.setParams(mapParams)
      case _ =>
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    var creator = params.get(TaskConstant.REQUESTAPPLICATIONNAME).asInstanceOf[String]
    val source = params.getOrDefault(TaskConstant.SOURCE, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, String]]
    val executeApplicationName = params.get(TaskConstant.EXECUTEAPPLICATIONNAME).asInstanceOf[String]
    if (StringUtils.isEmpty(creator)) creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue
    //if (StringUtils.isEmpty(executeApplicationName)) throw new EntranceIllegalParamException(20006, "param executeApplicationName can not be empty or null")
    /* When the execution type is IDE, executionCode and scriptPath cannot be empty at the same time*/
    /*当执行类型为IDE的时候，executionCode和scriptPath不能同时为空*/
    if (EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue.equals(creator) && StringUtils.isEmpty(source.get(TaskConstant.SCRIPTPATH)) &&
      StringUtils.isEmpty(executionCode))
      throw new EntranceIllegalParamException(20007, "param executionCode and scriptPath can not be empty at the same time")
    var runType: String = null
    if (StringUtils.isNotEmpty(executionCode)) {
      runType = params.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isEmpty(runType)) runType = EntranceConfiguration.DEFAULT_RUN_TYPE.getValue
      //If formatCode is not empty, we need to format it(如果formatCode 不为空的话，我们需要将其进行格式化)
      if (formatCode) executionCode = format(executionCode)
      jobReq.setExecutionCode(executionCode)
    }
    val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(executeApplicationName)
    val runTypeLabel = labelBuilderFactory.createLabel[Label[_]](LabelKeyConstant.CODE_TYPE_KEY, runType)
    val userCreatorLabel = labelBuilderFactory.createLabel[Label[_]](LabelKeyConstant.USER_CREATOR_TYPE_KEY, umUser + "-" + creator)

    val labelList = new util.ArrayList[Label[_]](3)
    labelList.add(engineTypeLabel)
    labelList.add(runTypeLabel)
    labelList.add(userCreatorLabel)
    if (jobReq.getParams != null ) {
      val labelMap = params.getOrDefault(TaskConstant.LABELS, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, Object]]
      if (null != labelMap && !labelMap.isEmpty) {
        val list: util.List[Label[_]] = labelBuilderFactory.getLabels(labelMap.asInstanceOf[util.Map[String, AnyRef]])
        labelList.addAll(list)
      }
    }
    jobReq.setProgress("0.0")
    jobReq.setSource(source)
    //为了兼容代码，让engineType和runType都有同一个属性
    jobReq.setStatus(SchedulerEventState.Inited.toString)
    //封装Labels
    jobReq.setLabels(labelList)
    jobReq.setMetrics(new util.HashMap[String, Object]())
    jobReq.getMetrics.put(TaskConstant.ENTRANCEJOB_SUBMIT_TIME, new Date(System.currentTimeMillis))
    jobReq
  }

  private def buildLabel(labelMap: util.Map[String, Object]): util.Map[String, Label[_]] = {
    val labelKeyValueMap = new util.HashMap[String, Label[_]]()
    if (null != labelMap && !labelMap.isEmpty) {
      val list: util.List[Label[_]] = labelBuilderFactory.getLabels(labelMap.asInstanceOf[util.Map[String, AnyRef]])
      if (null != list) {
        list.asScala.filter(_ != null).foreach {
          label => labelKeyValueMap.put(label.getLabelKey, label)
        }
      }
    }
    labelKeyValueMap
  }

  //todo to format code using proper way
  private def format(code: String): String = code

}
