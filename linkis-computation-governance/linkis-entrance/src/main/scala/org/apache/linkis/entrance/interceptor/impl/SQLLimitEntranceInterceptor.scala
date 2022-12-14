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
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil

class SQLLimitEntranceInterceptor extends EntranceInterceptor {
  private val LIMIT_CREATORS = EntranceConfiguration.SQL_LIMIT_CREATOR.getValue

  override def apply(task: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val (user, creator) = LabelUtil.getUserCreator(task.getLabels)
    if (!LIMIT_CREATORS.contains(creator)) {
      logAppender.append(
        LogUtils.generateWarn(s"The code you submit will not be limited by the limit \n")
      )
      return task
    }
    val codeType = {
      val codeType = LabelUtil.getCodeType(task.getLabels)
      if (null != codeType) {
        codeType.toLowerCase()
      } else {
        ""
      }
    }
    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    task match {
      case jobRequest: JobRequest =>
        languageType match {
          case CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL =>
            val executionCode = jobRequest.getExecutionCode
            SQLExplain.dealSQLLimit(executionCode, jobRequest, logAppender)
          case _ =>
        }
        jobRequest
      case _ => task
    }
  }

}
