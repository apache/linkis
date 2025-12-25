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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.CodeAndRunTypeUtils.LANGUAGE_TYPE_AI_SQL
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration.{AI_SQL_CREATORS, AI_SQL_KEY, TASK_RETRY_CODE_TYPE, TASK_RETRY_SWITCH}
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils

import java.{lang, util}

/**
 * 任务重试拦截器 用于根据任务类型和配置，动态为任务添加重试开关 在任务提交前对作业参数进行预处理，决定是否启用重试功能
 */
class TaskRetryInterceptor extends EntranceInterceptor with Logging {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    // 获取AI SQL相关配置
    val aiSqlEnable: Boolean = "true".equals(AI_SQL_KEY.getValue)
    val supportAISQLCreator: String = AI_SQL_CREATORS.toLowerCase()

    // 从标签提取任务元信息
    val labels: util.List[Label[_]] = jobRequest.getLabels
    val codeType: String = LabelUtil.getCodeType(labels)
    val creator: String = LabelUtil.getUserCreatorLabel(labels).getCreator

    // 全局重试开关开启时处理
    if (TASK_RETRY_SWITCH.getValue) {
      val startMap: util.Map[String, AnyRef] = TaskUtils.getStartupMap(jobRequest.getParams)

      // 分类型处理：AI SQL任务或配置支持的任务类型
      if (LANGUAGE_TYPE_AI_SQL.equals(codeType)) {
        // AI SQL任务需同时满足功能启用和创建者权限
        if (aiSqlEnable && supportAISQLCreator.contains(creator.toLowerCase())) {
          logAppender.append(
            LogUtils.generateWarn(s"The AI SQL task will initiate a failed retry \n")
          )
          startMap.put(TASK_RETRY_SWITCH.key, TASK_RETRY_SWITCH.getValue.asInstanceOf[AnyRef])
        }
      } else if (TASK_RETRY_CODE_TYPE.contains(codeType)) {
        // 普通任务只需满足类型支持
        logAppender.append(
          LogUtils.generateWarn(s"The StarRocks task will initiate a failed retry \n")
        )
        startMap.put(TASK_RETRY_SWITCH.key, TASK_RETRY_SWITCH.getValue.asInstanceOf[AnyRef])
      }

      // 更新作业参数
      TaskUtils.addStartupMap(jobRequest.getParams, startMap)
    }

    jobRequest
  }

}
