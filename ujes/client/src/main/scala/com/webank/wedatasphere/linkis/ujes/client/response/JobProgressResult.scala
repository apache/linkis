/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.ujes.client.response

import java.util.{List, Map}

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import org.json4s._
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions._

/**
  * created by cooperyang on 2019/5/23.
  */
@DWSHttpMessageResult("/api/rest_j/v\\d+/entrance/(\\S+)/progress")
class JobProgressResult extends UJESJobResult {

  private var progress: Float = _
  private var progressInfo: List[Map[String, AnyRef]] = _
  private var progressInfos: Array[JobProgressInfo] = _

  private implicit val formats = DefaultFormats

  def setProgress(progress: Float) = this.progress = progress
  def getProgress = progress

  def setProgressInfo(progressInfo: List[Map[String, AnyRef]]) = {
    this.progressInfo = progressInfo
    progressInfos = progressInfo.map(map => read[JobProgressInfo](write(map.toMap))).toArray
  }
  def getProgressInfo = progressInfos

}
