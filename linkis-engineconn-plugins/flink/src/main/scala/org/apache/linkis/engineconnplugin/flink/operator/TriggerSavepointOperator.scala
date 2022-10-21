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

package org.apache.linkis.engineconnplugin.flink.operator

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager
import org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.flink.executor.FlinkOnceExecutor
import org.apache.linkis.manager.common.operator.Operator

import java.text.MessageFormat

class TriggerSavepointOperator extends Operator with Logging {

  override def getNames: Array[String] = Array("doSavepoint")

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    val savepoint = getAsThrow[String]("savepointPath")
    val mode = getAsThrow[String]("mode")
    logger.info(s"try to $mode savepoint with path $savepoint.")
    OnceExecutorManager.getInstance.getReportExecutor match {
      case flinkExecutor: FlinkOnceExecutor[_] =>
        val writtenSavepoint =
          flinkExecutor.getClusterDescriptorAdapter.doSavepoint(savepoint, mode)
        Map("writtenSavepoint" -> writtenSavepoint)
      case executor =>
        throw new JobExecutionException(
          MessageFormat.format(NOT_SUPPORT_SAVEPOTION.getErrorDesc, executor.getClass.getSimpleName)
        )
    }
  }

}
