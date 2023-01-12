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

package org.apache.linkis.entrance.interceptor

import org.apache.linkis.bml.client.BmlClientFactory
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.governance.common.entity.job.{JobRequest, OnceExecutorContent}
import org.apache.linkis.governance.common.utils.OnceExecutorContentUtils
import org.apache.linkis.governance.common.utils.OnceExecutorContentUtils.BmlResource
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.JobLabel
import org.apache.linkis.manager.label.entity.engine.{CodeLanguageLabel, EngineConnModeLabel}
import org.apache.linkis.manager.label.entity.engine.EngineConnMode._
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.server.BDPJettyServerHelper

import java.{lang, util}
import java.io.ByteArrayInputStream

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._

class OnceJobInterceptor extends EntranceInterceptor {

  private val onceModes =
    Array(Once_With_Cluster.toString, Once.toString, Computation_With_Once.toString)

  private val bmlClient = BmlClientFactory.createBmlClient(Utils.getJvmUser)

  /**
   * The apply function is to supplement the information of the incoming parameter task, making the
   * content of this task more complete. Additional information includes: database information
   * supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
   *
   * @param task
   * @param logAppender
   *   Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
   * @return
   */
  override def apply(task: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val existsOnceLabel = task.getLabels.exists {
      case e: EngineConnModeLabel => onceModes.contains(e.getEngineConnMode)
      case _ => false
    }
    if (!existsOnceLabel) return task
    val jobLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[JobLabel])
    jobLabel.setJobId(task.getId.toString)
    task.getLabels.add(jobLabel)
    val onceExecutorContent = new OnceExecutorContent
    val params = task.getParams

    onceExecutorContent.setSourceMap(task.getSource.map { case (k, v) =>
      k -> v.asInstanceOf[Object]
    })
    onceExecutorContent.setVariableMap(TaskUtils.getVariableMap(params))
    onceExecutorContent.setRuntimeMap(TaskUtils.getRuntimeMap(params))
    onceExecutorContent.setJobContent(getJobContent(task))
    onceExecutorContent.setExtraLabels(new util.HashMap[String, AnyRef]) // TODO Set it if needed
    val contentMap = OnceExecutorContentUtils.contentToMap(onceExecutorContent)
    val bytes = BDPJettyServerHelper.jacksonJson.writeValueAsBytes(contentMap)
    val response = bmlClient.uploadResource(
      task.getExecuteUser,
      getFilePath(task),
      new ByteArrayInputStream(bytes)
    )
    val value =
      OnceExecutorContentUtils.resourceToValue(BmlResource(response.resourceId, response.version))
    TaskUtils.addStartupMap(
      params,
      Map(OnceExecutorContentUtils.ONCE_EXECUTOR_CONTENT_KEY -> value.asInstanceOf[Object])
    )
    task
  }

  protected def getFilePath(task: JobRequest): String =
    s"/tmp/${task.getExecuteUser}/${task.getId}"

  protected def getJobContent(task: JobRequest): util.Map[String, AnyRef] = {
    // TODO Wait for optimizing since the class `JobRequest` is waiting for optimizing .
    val jobContent = new util.HashMap[String, AnyRef]
    jobContent.put(TaskConstant.CODE, task.getExecutionCode)
    task.getLabels.foreach {
      case label: CodeLanguageLabel =>
        jobContent.put(TaskConstant.RUNTYPE, label.getCodeType)
      case _ =>
    }
    jobContent
  }

}
