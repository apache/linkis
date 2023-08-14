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

package org.apache.linkis.engineconnplugin.flink.listener

import org.apache.linkis.common.listener.{Event, EventListener}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconnplugin.flink.listener.RowsType.RowsType

trait FlinkListener extends EventListener {
  override def onEventError(event: Event, t: Throwable): Unit = {}
}

trait FlinkStatusListener extends FlinkListener {

  def onSuccess(rows: Int, rowsType: RowsType): Unit

  def onFailed(message: String, t: Throwable, rowsType: RowsType): Unit

}

trait InteractiveFlinkStatusListener extends FlinkStatusListener with Logging {

  private var isMarked = false
  private var cachedRows: Int = _

  def markSuccess(rows: Int): Unit = {
    isMarked = true
    cachedRows = rows
  }

  override final def onFailed(message: String, t: Throwable, rowsType: RowsType): Unit =
    if (isMarked) {
      logger.info("Ignore this error, since we have marked this job as succeed.", t)
      onSuccess(cachedRows, rowsType)
      // isMarked = false
    } else tryFailed(message, t)

  def tryFailed(message: String, throwable: Throwable): Unit

}

object RowsType extends Enumeration {
  type RowsType = Value
  val Fetched, Affected = Value
}

trait FlinkStreamingResultSetListener extends FlinkListener {

  def onResultSetPulled(rows: Int): Unit

}
