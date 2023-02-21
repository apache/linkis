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
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.CodeCheckException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil

class SparkCodeCheckInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    languageType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SCALA =>
        val codeBuilder: StringBuilder = new StringBuilder()
        val isAuth = SparkExplain.authPass(jobRequest.getExecutionCode, codeBuilder)
        if (!isAuth) {
          throw CodeCheckException(20050, "spark code check failed")
        }
      case _ =>
    }
    jobRequest
  }

}
