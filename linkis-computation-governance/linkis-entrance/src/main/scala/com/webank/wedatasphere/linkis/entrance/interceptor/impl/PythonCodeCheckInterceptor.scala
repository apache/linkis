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

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.PythonCodeCheckException
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil
import org.apache.commons.lang.exception.ExceptionUtils

/**
  * Description: Check for python code, prohibiting the use of sys, os, and creating processes(用于python代码的检查，禁止使用sys、os以及创建进程等行为)
  */
class PythonCodeCheckInterceptor extends EntranceInterceptor with Logging {
  override def apply(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
    val errorBuilder = new StringBuilder
    codeType match {
        case "python" | "pyspark" | "py" =>
        Utils.tryThrow{
          if (PythonExplain.authPass(jobRequest.getExecutionCode, errorBuilder)) {
            jobRequest
          } else {
            val msg = s"check python auth failed. ${errorBuilder.toString()}"
            error(msg)
            jobRequest.setErrorCode(20073)
            jobRequest.setErrorDesc(msg)
            throw PythonCodeCheckException(20073, msg)
          }
        } {
            case PythonCodeCheckException(errCode, errDesc) =>
            jobRequest.setErrorCode(errCode)
            jobRequest.setErrorDesc(errDesc)
              PythonCodeCheckException(errCode, errDesc)
            case t: Throwable =>
              val exception = PythonCodeCheckException(20073, "Checking python code failed!(检查python代码失败！)" + ExceptionUtils.getRootCauseMessage(t))
              exception.initCause(t)
              exception
          }
      case _ => jobRequest
      }
  }
}
