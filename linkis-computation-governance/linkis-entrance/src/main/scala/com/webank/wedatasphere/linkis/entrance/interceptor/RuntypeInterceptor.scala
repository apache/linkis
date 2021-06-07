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

package com.webank.wedatasphere.linkis.entrance.interceptor

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil

/**
  * Description: this interceptor is used to complete code with run type for
  * further use in engine
  */
class RuntypeInterceptor extends EntranceInterceptor with Logging {

  override def apply(task: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val codeType = LabelUtil.getCodeType(task.getLabels)
    codeType match {
      case "python" | "py" | "pyspark" => val code = task.getExecutionCode
        task.setExecutionCode("%python\n" + code)
        task
      case "sql" | "hql" =>
        val code = task.getExecutionCode
        task.setExecutionCode("%sql\n" + code)
        task
      case "scala" =>
        val code = task.getExecutionCode
        task.setExecutionCode("%scala\n" + code)
        task
      case _ =>
        error(s"Invalid codeType ${codeType}")
        task
    }
  }

}