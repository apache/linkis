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

package org.apache.linkis.engineconn.once.executor.operator

import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconn.once.executor.OperableOnceExecutor
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager
import org.apache.linkis.manager.common.operator.{Operator, OperatorFactory}


class OperableOnceEngineConnOperator extends Operator {

  import OperableOnceEngineConnOperator._

  override def getNames: Array[String] = Array(PROGRESS_OPERATOR_NAME, METRICS_OPERATOR_NAME, DIAGNOSIS_OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    val operatorName = OperatorFactory().getOperatorName(parameters)
    OnceExecutorManager.getInstance.getReportExecutor match {
      case operableOnceExecutor: OperableOnceExecutor =>
        operatorName match {
          case PROGRESS_OPERATOR_NAME =>
            val progressInfo = operableOnceExecutor.getProgressInfo
            val progressInfoMap = if (progressInfo != null && progressInfo.nonEmpty) {
              progressInfo.map(progressInfo => Map("id" -> progressInfo.id, "totalTasks" -> progressInfo.totalTasks,
                "runningTasks" -> progressInfo.runningTasks, "failedTasks" -> progressInfo.failedTasks, "succeedTasks" -> progressInfo.succeedTasks))
            } else Array.empty[Map[String, Any]]
            Map("progress" -> operableOnceExecutor.getProgress, "progressInfo" -> progressInfoMap)
          case METRICS_OPERATOR_NAME =>
            Map("metrics" -> operableOnceExecutor.getMetrics)
          case DIAGNOSIS_OPERATOR_NAME =>
            Map("diagnosis" -> operableOnceExecutor.getDiagnosis)
          case _ =>
            throw EngineConnException(20308, s"This engineConn don't support $operatorName operator.")
        }
      case _ => throw EngineConnException(20308, s"This engineConn don't support $operatorName operator.")
    }
  }

}
object OperableOnceEngineConnOperator {
  val PROGRESS_OPERATOR_NAME = "engineConnProgress"
  val METRICS_OPERATOR_NAME = "engineConnMetrics"
  val DIAGNOSIS_OPERATOR_NAME = "engineConnDiagnosis"
}