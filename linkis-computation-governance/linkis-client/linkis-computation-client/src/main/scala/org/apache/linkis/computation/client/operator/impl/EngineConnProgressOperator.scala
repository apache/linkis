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

package org.apache.linkis.computation.client.operator.impl

import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.operator.OnceJobOperator
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.util

import scala.collection.JavaConverters._

class EngineConnProgressOperator extends OnceJobOperator[EngineConnProgressInfo] {

  override def getName: String = EngineConnProgressOperator.OPERATOR_NAME

  override protected def resultToObject(result: EngineConnOperateResult): EngineConnProgressInfo = {
    val progressInfoList: util.ArrayList[util.Map[String, AnyRef]] = result.getAs("progressInfo")
    val progressInfo = progressInfoList.asScala
      .map(map =>
        JobProgressInfo(
          map.get("id").asInstanceOf[String],
          map.get("totalTasks").asInstanceOf[Int],
          map.get("runningTasks").asInstanceOf[Int],
          map.get("failedTasks").asInstanceOf[Int],
          map.get("succeedTasks").asInstanceOf[Int]
        )
      )
      .toArray
    EngineConnProgressInfo(result.getAs[Double]("progress").toFloat, progressInfo)
  }

}

object EngineConnProgressOperator {
  val OPERATOR_NAME = "engineConnProgress"
}

case class EngineConnProgressInfo(progress: Float, progressInfo: Array[JobProgressInfo])
