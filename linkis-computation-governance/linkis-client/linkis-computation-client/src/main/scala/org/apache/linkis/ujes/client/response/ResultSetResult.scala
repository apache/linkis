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

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult
import org.apache.linkis.ujes.client.request.UserAction
import org.apache.linkis.ujes.client.utils.UJESClientUtils.evaluate

import java.util

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/filesystem/openFile")
class ResultSetResult extends DWSResult with UserAction {

  private var `type`: String = _

  private var metadataList: util.List[util.Map[String, String]] = _

  private var fileContentList: util.List[util.ArrayList[_]] = _

  def getMetadataList: util.List[util.Map[String, String]] = {
    metadata.asInstanceOf[util.List[util.Map[String, String]]]
  }

  def getRowList: util.List[util.ArrayList[Any]] = {
    val metaData = metadata.asInstanceOf[util.List[util.Map[String, String]]]
    val fileContentList = fileContent.asInstanceOf[util.List[util.ArrayList[Any]]]
    for (metaDataColnum <- 1 to metaData.size()) {
      val col = metaData.get(metaDataColnum - 1)
      if (!col.get("dataType").equals("string")) {
        for (cursor <- 1 to fileContentList.size()) {
          val colDataList = fileContentList.get(cursor - 1)
          var colData = colDataList.get(metaDataColnum - 1)
          colData = evaluate(col.get("dataType"), colData.toString)
          colDataList.set(metaDataColnum - 1, colData)
        }
      }
    }
    fileContentList
  }

  def setType(`type`: String): Unit = this.`type` = `type`
  def getType: String = `type`

  @BeanProperty
  var metadata: Object = _

  @BeanProperty
  var page: Int = _

  @BeanProperty
  var totalLine: Int = _

  @BeanProperty
  var totalPage: Int = _

  @BeanProperty var fileContent: Object = _
}
