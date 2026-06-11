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

import org.apache.linkis.common.utils.{CodeAndRunTypeUtils, Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.CodeCheckException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.utils.LabelUtil

import org.apache.commons.lang3.StringUtils

class SQLCodeCheckInterceptor extends EntranceInterceptor with Logging {

  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val codeType = {
      val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
      if (null != codeType) {
        codeType.toLowerCase()
      } else {
        ""
      }
    }
    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    languageType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL =>
        val sb: StringBuilder = new StringBuilder
        val isAuth: Boolean = SQLExplain.authPass(jobRequest.getExecutionCode, sb)
        if (!isAuth) {
          throw CodeCheckException(20051, "sql code check failed, reason is " + sb.toString())
        }

        // Hive LOCATION control check
        // Only check if: 1. Hive engine 2. Feature enabled 3. Creator NOT in whitelist
        val engineType = LabelUtil.getEngineTypeLabel(jobRequest.getLabels).getEngineType
        if (
            EngineType.HIVE.toString.equalsIgnoreCase(engineType) &&
            EntranceConfiguration.HIVE_LOCATION_CONTROL_ENABLE.getValue &&
            !isCreatorWhitelisted(LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getCreator)
        ) {
          val locationSb: StringBuilder = new StringBuilder
          SQLExplain.checkLocation(jobRequest.getExecutionCode, locationSb)
          if (locationSb.nonEmpty) {
            throw CodeCheckException(20052, locationSb.toString())
          }
        }
      case _ =>
    }
    jobRequest

  }

  /**
   * Check if the creator is in the LOCATION control whitelist
   *
   * @param creator
   *   the application creator name
   * @return
   *   true if the creator is whitelisted (LOCATION allowed), false otherwise
   */
  private def isCreatorWhitelisted(creator: String): Boolean = {
    if (StringUtils.isBlank(creator)) {
      return false
    }
    val whitelist = EntranceConfiguration.HIVE_LOCATION_CONTROL_WHITELIST_CREATORS.getValue
    if (StringUtils.isBlank(whitelist)) {
      return false
    }
    whitelist.split(",").map(_.trim).exists(_.equalsIgnoreCase(creator))
  }

}
