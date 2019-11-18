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

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.EntranceIllegalParamException
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

/**
  * created by enjoyyin on 2018/12/7
  * Description:
  */
class CommonEntranceParser extends AbstractEntranceParser{

  private val logger = LoggerFactory.getLogger(classOf[CommonEntranceParser])
  /**
    * parse params to be a task
    * params json data from frontend
    *
    */
  override def parseToTask(params: util.Map[String, Any]): Task = {
    val task = new RequestPersistTask
    task.setCreatedTime(new Date(System.currentTimeMillis))
    task.setInstance(Sender.getThisInstance)
    val umUser = params.get(TaskConstant.UMUSER).asInstanceOf[String]
    if (umUser == null) throw new EntranceIllegalParamException(20005, "umUser can not be null")
    task.setUmUser(umUser)
    var executionCode = params.get(TaskConstant.EXECUTIONCODE).asInstanceOf[String]
    val _params = params.get(TaskConstant.PARAMS)
    _params match {
      case mapParams:java.util.Map[String, Object] => task.setParams(mapParams)
      case _ =>
    }
    val formatCode = params.get(TaskConstant.FORMATCODE).asInstanceOf[Boolean]
    var creator = params.get(TaskConstant.REQUESTAPPLICATIONNAME).asInstanceOf[String]
    val scriptPath = params.get(TaskConstant.SCRIPTPATH).asInstanceOf[String]
    val source = params.getOrDefault(TaskConstant.SOURCE,new util.HashMap[String,String]()).asInstanceOf[util.Map[String,String]]
    val executeApplicationName = params.get(TaskConstant.EXECUTEAPPLICATIONNAME).asInstanceOf[String]
    if (StringUtils.isEmpty(creator)) creator = EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue
    if (StringUtils.isEmpty(executeApplicationName)) throw new EntranceIllegalParamException(20006, "param executeApplicationName can not be empty or null")
    /* When the execution type is IDE, executionCode and scriptPath cannot be empty at the same time*/
    /*当执行类型为IDE的时候，executionCode和scriptPath不能同时为空*/
    if (EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue.equals(creator) && StringUtils.isEmpty(scriptPath) &&
      StringUtils.isEmpty(executionCode))
      throw new EntranceIllegalParamException(20007, "param executionCode and scriptPath can not be empty at the same time")
    var runType:String = null
    if (StringUtils.isNotEmpty(executionCode)) {
      runType = params.get(TaskConstant.RUNTYPE).asInstanceOf[String]
      if (StringUtils.isEmpty(runType)) runType = EntranceConfiguration.DEFAULT_RUN_TYPE.getValue
      //If formatCode is not empty, we need to format it(如果formatCode 不为空的话，我们需要将其进行格式化)
      if (formatCode) executionCode = format(executionCode)
      task.setExecutionCode(executionCode)
    }
    if (source.isEmpty) source.put(TaskConstant.SCRIPTPATH,scriptPath)
    task.setSource(source)
    if (StringUtils.isNotEmpty(scriptPath)) {
      task.setScriptPath(scriptPath)
//      val strings = StringUtils.split(scriptPath, ".")
//      if (strings.length >= 2) {
//        /*获得执行脚本文件的后缀名,如果不能从一个执行脚本的后缀名判断出类型，可能需要报错*/
//        //todo If you can't get it from the file suffix name(如果不能从文件后缀名得到)
//        runType = strings(strings.length - 1)
//        if (ParserUtils.getCorrespondingType(runType) != null) runType = ParserUtils.getCorrespondingType(runType)
//        else logger.warn("未能找到相应的执行类型:" + runType)
//      }
//      else logger.warn("scriptPath:" + scriptPath + "没有后缀名, 无法判断任务是哪种类型")
    }
    task.setEngineType(runType)
    //为了兼容代码，让engineType和runType都有同一个属性
    task.setRunType(runType)
    task.setExecuteApplicationName(executeApplicationName)
    task.setRequestApplicationName(creator)
    task.setStatus(SchedulerEventState.Inited.toString)
    task
  }
  //todo to format code using proper way
  private def format(code:String):String = code

}
