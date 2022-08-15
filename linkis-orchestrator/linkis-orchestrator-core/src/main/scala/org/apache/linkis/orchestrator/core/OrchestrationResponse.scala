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

package org.apache.linkis.orchestrator.core

import org.apache.linkis.common.io.FsPath

/**
 */
trait OrchestrationResponse

trait CompletedOrchestrationResponse extends OrchestrationResponse

trait SucceedOrchestrationResponse extends CompletedOrchestrationResponse

trait ResultSetOrchestrationResponse extends SucceedOrchestrationResponse {
  def getResultSet: String
}

trait ResultSetPathOrchestrationResponse extends SucceedOrchestrationResponse {
  def getResultSetPath: FsPath
}

case class ResultSet(result: String, alias: String)

trait ArrayResultSetOrchestrationResponse extends SucceedOrchestrationResponse {
  def getResultSets: Array[ResultSet]
}

trait FailedOrchestrationResponse extends CompletedOrchestrationResponse {

  def getErrorMsg: String

  def getErrorCode: Int

}
