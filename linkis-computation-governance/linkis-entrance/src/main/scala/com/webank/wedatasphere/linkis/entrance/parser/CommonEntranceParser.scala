/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.parser

import java.util
import java.util.Date

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.{EntranceErrorCode, EntranceIllegalParamException}
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.manager.label.builder.factory.{LabelBuilderFactory, LabelBuilderFactoryContext, StdLabelBuilderFactory}
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineRunTypeLabel, EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtils}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConverters._


class CommonEntranceParser extends AbstractEntranceParser with Logging {

  private val labelBuilderFactory: LabelBuilderFactory = new StdLabelBuilderFactory

  /**
    * parse params to be a task
    * params json data from frontend
    *
    */
  override def parseToTask(params: util.Map[String, Any]): Task = {
    if (!params.containsKey(TaskConstant.EXECUTION_CONTENT)) {
      return parseToOldTask(params)
    }

    val task = new RequestPersistTask
    task.setCreatedTime(new Date(System.currentTimeMillis))
    task.setInstance(Sender.getThisInstance)
    val umUser = params.get(TaskConstant.EXECUTE_USER).asInstanceOf[String]
    val submitUser = params.get(TaskConstant.SUBMIT_USER).asInstanceOf[String]
    task.setSubmitUser(submitUser)
    if (StringUtils.isBlank(umUser)) task.setUmUser(submitUser)
    task.setUmUser(umUser)
    val executionContent = params.getOrDefault(TaskConstant.EXECUTION_CONTENT, null).asInstanceOf[util.Map[String, Object]]
    val configMap = params.getOrDefault(TaskConstant.PARAMS, null).asInstanceOf[util.Map[String, Object]]
    val labelMap = params.getOrDefault(TaskConstant.LABELS, null).asInstanceOf[util.Map[String, Object]]
    val source = params.getOrDefault(TaskConstant.SOURCE, new util.HashMap[String, String]()).asInstanceOf[util.Map[String, String]]
    val sourcePath = source.getOrDefault(TaskConstant.SCRIPTPATH, null)
    if (null == sourcePath || null == labelMap) {
      throw new EntranceIllegalParamException(EntranceErrorCode.PARAM_CANNOT_EMPTY.getErrCode, EntranceErrorCode.PARAM_CANNOT_EMPTY.getDesc + s", source : ${sourcePath}, labels : ${BDPJettyServerHelper.gson.toJson(labelMap)}")
    }

    var code: String = null
    var runType: String = null
    if (executionContent.containsKey(TaskConstant.CODE)) {
      code = executionContent.get(TaskConstant.CODE).asInstanceOf[String]
      runType = executionContent.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isEmpty(code))
        throw new EntranceIllegalParamException(20007, "param executionCode and scriptPath can not be empty at the same time")
    } else {
      // todo check
      throw new EntranceIllegalParamException(20010, "Only code with runtype supported !")
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    if (formatCode) code = format(code)
    task.setExecutionCode(code)
    var labels: util.Map[String, Label[_]] = buildLabel(labelMap)
    val engineTypeLabel = labels.getOrElse(LabelKeyConstant.ENGINE_TYPE_KEY, null).asInstanceOf[EngineTypeLabel]
    if (null == engineTypeLabel) {
      val msg = s"Cannot create engineTypeLabel, labelMap : ${BDPJettyServerHelper.gson.toJson(labelMap)}"
      error(msg)
      throw new EntranceIllegalParamException(EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        EntranceErrorCode.LABEL_PARAMS_INVALID.getDesc + msg)
    } else {
      task.setEngineType(engineTypeLabel.getEngineType)
    }
    val engineRunTypeLabel = labels.getOrElse(LabelKeyConstant.ENGINE_RUN_TYPE_KEY, null).asInstanceOf[EngineRunTypeLabel]
    if (null != engineRunTypeLabel) {
      task.setRunType(engineRunTypeLabel.getRunType)
    } else {
      warn(s"RunType not set. EngineType : ${engineTypeLabel.getEngineType}")
    }
    var userCreatorLabel = labels.getOrElse(LabelKeyConstant.USER_CREATOR_TYPE_KEY, null).asInstanceOf[UserCreatorLabel]

    if (null == userCreatorLabel) {
      userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
      val creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue
      userCreatorLabel.setUser(umUser)
      userCreatorLabel.setCreator(creator)
      labels.put(userCreatorLabel.getLabelKey, userCreatorLabel)
    }
    labels += (LabelKeyConstant.ENGINE_TYPE_KEY -> engineTypeLabel)
    labels += (LabelKeyConstant.ENGINE_RUN_TYPE_KEY -> engineRunTypeLabel)
    labels += (LabelKeyConstant.USER_CREATOR_TYPE_KEY -> userCreatorLabel)
    task.setLabels(new util.ArrayList[Label[_]](labels.values()))
    task.setSource(source)
    task.setExecuteApplicationName(engineTypeLabel.getEngineType)
    task.setRequestApplicationName(userCreatorLabel.getCreator)
    task.setStatus(SchedulerEventState.Inited.toString)
    task.setCreateService(EntranceConfiguration.DEFAULT_CREATE_SERVICE.getValue)
    task.setParams(configMap)
    task
  }

  private def parseToOldTask(params: util.Map[String, Any]): Task = {

    val task = new RequestPersistTask
    task.setCreatedTime(new Date(System.currentTimeMillis))
    task.setInstance(Sender.getThisInstance)
    val umUser = params.get(TaskConstant.UMUSER).asInstanceOf[String]
    val submitUser = params.get(TaskConstant.SUBMIT_USER).asInstanceOf[String]
    task.setSubmitUser(submitUser)
    if (umUser == null) throw new EntranceIllegalParamException(20005, "umUser can not be null")
    task.setUmUser(umUser)
    var executionCode = params.get(TaskConstant.EXECUTIONCODE).asInstanceOf[String]
    val _params = params.get(TaskConstant.PARAMS)
    _params match {
      case mapParams: java.util.Map[String, Object] => task.setParams(mapParams)
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
      task.setExecutionCode(executionCode)
    }
    task.setSource(source)
    task.setEngineType(runType)
    //为了兼容代码，让engineType和runType都有同一个属性
    task.setRunType(runType)
    task.setExecuteApplicationName(executeApplicationName)
    task.setRequestApplicationName(creator)
    task.setStatus(SchedulerEventState.Inited.toString)
    task.setCreateService(EntranceConfiguration.DEFAULT_CREATE_SERVICE.getValue)
    //封装Labels
    task.setLabels(buildLabel(task))
    task
  }

  private def buildLabel(task: RequestPersistTask): util.List[Label[_]] = {
    val labelsList = new util.ArrayList[Label[_]]()
    val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory
    if (StringUtils.isNotBlank(task.getUmUser) && StringUtils.isNotBlank(task.getRequestApplicationName)) {
      val userCreatorLabel = labelBuilderFactory.createLabel(classOf[UserCreatorLabel])
      userCreatorLabel.setCreator(task.getRequestApplicationName)
      userCreatorLabel.setUser(task.getUmUser)
      labelsList.add(userCreatorLabel)
    }

    if (StringUtils.isNotBlank(task.getExecuteApplicationName)) {
      val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(task.getExecuteApplicationName)
      labelsList.add(engineTypeLabel)
    }

    if (StringUtils.isNotBlank(task.getRunType)) {
      val runTypeLabel = labelBuilderFactory.createLabel(classOf[EngineRunTypeLabel])
      runTypeLabel.setRunType(task.getRunType)
      labelsList.add(runTypeLabel)
    }

    val labels = TaskUtils.getLabelsMap(task.getParams.asInstanceOf[util.Map[String, Any]])
    if (null != labels) {
      LabelUtils.distinctLabel(labelBuilderFactory.getLabels(labels.asInstanceOf[util.Map[String, AnyRef]]), labelsList)
    } else {
      labelsList
    }
  }

  private def buildLabel(labelMap: util.Map[String, Object]): util.Map[String, Label[_]] = {
    val labelKeyValueMap = new util.HashMap[String, Label[_]]()
    if (null != labelMap && !labelMap.isEmpty) {
      val list: util.List[Label[_]] = labelBuilderFactory.getLabels(labelMap.asInstanceOf[util.Map[String, AnyRef]])
      if (null != list) {
        list.asScala.foreach {
          label => labelKeyValueMap.put(label.getLabelKey, label)
        }
      }
    }
    labelKeyValueMap
  }

  //todo to format code using proper way
  private def format(code: String): String = code

}
