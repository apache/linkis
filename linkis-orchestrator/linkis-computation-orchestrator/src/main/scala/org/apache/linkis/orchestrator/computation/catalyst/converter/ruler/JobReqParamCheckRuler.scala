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

package org.apache.linkis.orchestrator.computation.catalyst.converter.ruler

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.exception.{
  OrchestratorErrorCodeSummary,
  OrchestratorErrorException
}
import org.apache.linkis.orchestrator.extensions.catalyst.ConverterCheckRuler
import org.apache.linkis.orchestrator.plans.ast.ASTContext

import org.apache.commons.lang3.StringUtils

/**
 */

class JobReqParamCheckRuler extends ConverterCheckRuler with Logging {

  override def apply(in: JobReq, context: ASTContext): Unit = {
    val executeUser = in.getExecuteUser
    val param = in.getParams
    if (StringUtils.isEmpty(executeUser)) {
      throw new OrchestratorErrorException(
        OrchestratorErrorCodeSummary.JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE,
        s"job:${in.getName} execute user is null, please check request again!"
      )
    }
  }

  override def getName: String = {
    val className = getClass.getName
    if (className endsWith "$") className.dropRight(1) else className
  }

}
