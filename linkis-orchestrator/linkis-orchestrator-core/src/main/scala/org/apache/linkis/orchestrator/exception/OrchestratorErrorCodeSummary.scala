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
 
package org.apache.linkis.orchestrator.exception

/**
  *
  */
object OrchestratorErrorCodeSummary {

  val LABEL_NOT_EXISTS_ERROR_CODE = 21001

  val CONVERTER_FOR_NOT_SUPPORT_ERROR_CODE = 21100
  val PARSER_FOR_NOT_SUPPORT_ERROR_CODE = 21101
  val PLANNER_FOR_NOT_SUPPORT_ERROR_CODE = 21102
  val OPTIMIZER_FOR_NOT_SUPPORT_ERROR_CODE = 21103


  val ORCHESTRATION_FOR_RESPONSE_NOT_SUPPORT_ERROR_CODE = 21200
  val ORCHESTRATION_FOR_OPERATION_NOT_SUPPORT_ERROR_CODE = 21201

  val JOB_REQUEST_PARAM_ILLEGAL_ERROR_CODE = 21300
  val EXECUTION_FOR_EXECUTION_ERROR_CODE = 21301
  val METHOD_NUT_SUPPORT_CODE = 21302
  val JOB_INFO_INCOMPLETE_ERROR_CODE = 21303

  val STAGE_ERROR_CODE = 21304

  val EXECUTION_ERROR_CODE = 21304

  val JOB_REUSE_SAME_ENGINE_ERROR = 21305

  val JOB_LABEL_CONFLICT_ERROR = 21306

  val EXECUTION_FATAL_CODE = 21000

}
