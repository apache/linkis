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

import org.apache.linkis.common.utils.CodeAndRunTypeUtils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.CodeCheckException
import org.apache.linkis.entrance.utils.EntranceUtils
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil

import org.apache.commons.lang3.StringUtils

import java.lang

class SensitiveCheckInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    if (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_SWITCH) {
      return jobRequest
    }

    val labellist = jobRequest.getLabels
    val codeType = Option(LabelUtil.getCodeType(labellist))
      .map(_.toLowerCase())
      .getOrElse("")

    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    if (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_RUNTYPE.contains(languageType)) {
      return jobRequest
    }

    val creator = LabelUtil.getUserCreatorLabel(labellist).getCreator
    if (
        StringUtils.isNotBlank(
          EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_CREATOR
        ) && (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_CREATOR.contains(creator))
    ) {
      return jobRequest
    }

    val engineType = LabelUtil.getEngineTypeLabel(labellist).getEngineType
    if (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_ENGINETYPE.contains(engineType)) {
      return jobRequest
    }

    // 检查执行用户和提交用户
    checkUserSensitivity(jobRequest.getExecuteUser, jobRequest, engineType, logAppender)
    checkUserSensitivity(jobRequest.getSubmitUser, jobRequest, engineType, logAppender)

    jobRequest
  }

  /**
   * 检查用户敏感信息
   */
  private def checkUserSensitivity(
      user: String,
      jobRequest: JobRequest,
      engineType: String,
      logAppender: lang.StringBuilder
  ): Unit = {
    val departmentId = EntranceUtils.getUserDepartmentId(user)
    val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
    if (EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_DEPARTMENT.contains(departmentId)) {
      val (result, reason) =
        EntranceUtils.sensitiveSqlCheck(
          jobRequest.getExecutionCode,
          codeType,
          engineType,
          user,
          logAppender
        )
      if (result && !EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_WHITELIST.contains(user)) {
        throw CodeCheckException(20054, "当前操作涉及明文信息读取，禁止执行该操作, 原因：" + reason)
      }
    }
  }

}
