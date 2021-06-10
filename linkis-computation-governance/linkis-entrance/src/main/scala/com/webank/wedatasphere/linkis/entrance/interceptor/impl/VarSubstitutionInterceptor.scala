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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import com.webank.wedatasphere.linkis.common.exception.ErrorException
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.VarSubstitutionException
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil
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
          val engineType = LabelUtil.getEngineType(jobRequest.getLabels)
          val (result, code) = CustomVariableUtils.replaceCustomVar(jobRequest, engineType)
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