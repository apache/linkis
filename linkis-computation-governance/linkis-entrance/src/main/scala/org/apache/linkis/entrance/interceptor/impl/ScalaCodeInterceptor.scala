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

import org.apache.linkis.common.utils.{CodeAndRunTypeUtils, Utils}
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.ScalaCodeCheckException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil

import java.lang

class ScalaCodeInterceptor extends EntranceInterceptor {

  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
    val errorBuilder = new StringBuilder("")
    languageType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SCALA =>
        Utils.tryThrow(ScalaExplain.authPass(jobRequest.getExecutionCode, errorBuilder)) {
          case ScalaCodeCheckException(errorCode, errDesc) =>
            jobRequest.setErrorCode(errorCode)
            jobRequest.setErrorDesc(errDesc)
            ScalaCodeCheckException(errorCode, errDesc)
          case t: Throwable =>
            val exception = ScalaCodeCheckException(20074, "Scala code check failed(scala代码检查失败)")
            exception.initCause(t)
            exception
        }
      case _ =>
    }
    jobRequest
  }

}
