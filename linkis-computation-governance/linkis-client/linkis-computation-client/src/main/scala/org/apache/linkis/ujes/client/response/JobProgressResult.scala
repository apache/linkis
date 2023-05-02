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

package org.apache.linkis.ujes.client.response

import org.apache.linkis.common.utils.JsonUtils
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.util

import scala.collection.JavaConverters._

@DWSHttpMessageResult("/api/rest_j/v\\d+/entrance/(\\S+)/progress")
class JobProgressResult extends UJESJobResult {

  private var progress: Float = _
  private var progressInfo: util.List[util.Map[String, AnyRef]] = _
  private var progressInfos: Array[JobProgressInfo] = _

  def setProgress(progress: Float): Unit = this.progress = progress
  def getProgress: Float = progress

  def setProgressInfo(progressInfo: util.List[util.Map[String, AnyRef]]): Unit = {
    this.progressInfo = progressInfo
    progressInfos = progressInfo.asScala
      .map(map =>
        JsonUtils.jackson
          .readValue(
            JsonUtils.jackson.writeValueAsString(map.asScala.toMap),
            classOf[JobProgressInfo]
          )
      )
      .toArray
  }

  def getProgressInfo: Array[JobProgressInfo] = progressInfos

}
