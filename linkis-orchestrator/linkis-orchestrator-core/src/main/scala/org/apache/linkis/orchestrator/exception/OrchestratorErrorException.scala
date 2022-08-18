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

package org.apache.linkis.orchestrator.exception

import org.apache.linkis.common.exception.ErrorException

/**
 */
class OrchestratorErrorException(errorCode: Int, errorMsg: String)
    extends ErrorException(errorCode, errorMsg) {

  def this(errorCode: Int, errorMsg: String, t: Throwable) = {
    this(errorCode, errorMsg)
    initCause(t)
  }

}

class OrchestratorValidateFailedException(errorMsg: String)
    extends OrchestratorErrorException(
      OrchestratorErrorCodeSummary.LABEL_NOT_EXISTS_ERROR_CODE,
      errorMsg
    )

class OrchestratorUseSameEngineException(errorMsg: String)
    extends OrchestratorErrorException(
      OrchestratorErrorCodeSummary.JOB_REUSE_SAME_ENGINE_ERROR,
      errorMsg
    )

class OrchestratorLabelConflictException(errorMsg: String)
    extends OrchestratorErrorException(
      OrchestratorErrorCodeSummary.JOB_LABEL_CONFLICT_ERROR,
      errorMsg
    )
