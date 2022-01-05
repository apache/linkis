/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.VarSubstitutionException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.commons.lang.exception.ExceptionUtils

/**
  * Description: For variable substitution(用于变量替换)
  */
class VarSubstitutionInterceptor extends EntranceInterceptor {

  @throws[ErrorException]
  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    jobRequest match {
      case jobRequest: JobRequest =>
        Utils.tryThrow {
          logAppender.append(LogUtils.generateInfo("Program is substituting variables for you") + "\n")
          val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
          val (result, code) = CustomVariableUtils.replaceCustomVar(jobRequest, codeType)
          if (result) jobRequest.setExecutionCode(code)
          logAppender.append(LogUtils.generateInfo("Variables substitution ended successfully") + "\n")
          jobRequest
        } {
          case e: VarSubstitutionException =>
            val exception = VarSubstitutionException(20050, "Variable replacement failed!(变量替换失败！)" + ExceptionUtils.getRootCauseMessage(e))
            exception.initCause(e)
            exception
          case t: Throwable =>
            val exception = VarSubstitutionException(20050, "Variable replacement failed!(变量替换失败！)" + ExceptionUtils.getRootCauseMessage(t))
            exception.initCause(t)
            exception
        }
      case _ => jobRequest
    }
  }
}