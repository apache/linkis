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

package org.apache.linkis.engineconn.acessible.executor.operator.impl

import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.YarnExecutor
import org.apache.linkis.manager.common.operator.Operator

class EngineConnApplicationInfoOperator extends Operator {

  override def getNames: Array[String] = Array(EngineConnApplicationInfoOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    ExecutorManager.getInstance.getReportExecutor match {
      case yarnExecutor: YarnExecutor =>
        Map(
          "applicationId" -> yarnExecutor.getApplicationId,
          "applicationUrl" -> yarnExecutor.getApplicationURL,
          "queue" -> yarnExecutor.getQueue,
          "yarnMode" -> yarnExecutor.getYarnMode
        )
      case _ =>
        throw EngineConnException(
          20301,
          "EngineConn is not a yarn application, cannot fetch applicaiton info."
        )
    }
  }

}

object EngineConnApplicationInfoOperator {
  val OPERATOR_NAME = "engineConnYarnApplication"
}
