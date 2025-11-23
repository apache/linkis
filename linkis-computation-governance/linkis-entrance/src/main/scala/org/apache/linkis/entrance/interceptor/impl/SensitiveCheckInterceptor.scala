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
import org.apache.linkis.common.utils.CodeAndRunTypeUtils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.CodeCheckException
import org.apache.linkis.entrance.utils.EntranceUtils
import org.apache.linkis.entrance.utils.EntranceUtils.logInfo
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil

import org.apache.commons.lang3.StringUtils

import java.lang

class SensitiveCheckInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    if (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_SWITCH) {
      return jobRequest
    }

    val isWhiteList = EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_WHITELIST.contains(
      jobRequest.getExecuteUser
    ) ||
      EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_WHITELIST.contains(jobRequest.getSubmitUser)
    if (isWhiteList) {
      logAppender.append(
        LogUtils
          .generateInfo(s"Sensitive SQL Check: whiteList contains user ！ Skip Check\n")
      )
      return jobRequest
    }
    val labellist = jobRequest.getLabels

    val engineType = LabelUtil.getEngineTypeLabel(labellist).getEngineType
    if (!EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_ENGINETYPE.contains(engineType)) {
      return jobRequest
    }

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

    val executeUserDepartmentId = EntranceUtils.getUserDepartmentId(jobRequest.getExecuteUser)
    val submitUserDepartmentId = EntranceUtils.getUserDepartmentId(jobRequest.getSubmitUser)
    if (
        (StringUtils.isNotBlank(
          executeUserDepartmentId
        ) && EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_DEPARTMENT.contains(
          executeUserDepartmentId
        )) || (
          StringUtils.isNotBlank(
            submitUserDepartmentId
          ) && EntranceConfiguration.DOCTOR_SENSITIVE_SQL_CHECK_DEPARTMENT.contains(
            submitUserDepartmentId
          )
        )
    ) {
      val (result, reason) =
        EntranceUtils.sensitiveSqlCheck(
          jobRequest.getExecutionCode,
          languageType,
          engineType,
          jobRequest.getExecuteUser,
          logAppender
        )
      if (result) {
        throw CodeCheckException(20054, "当前操作涉及明文信息读取，禁止执行该操作, 原因：" + reason)
      }
    }
    jobRequest
  }

}
