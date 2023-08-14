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

package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo

import java.util

import scala.beans.BeanProperty

@DWSHttpMessageResult(
  "/api/rest_j/v\\d+/(metadataQuery|metadatamanager)/(getColumns|columns)(\\S+/db/\\S+/table/\\S+)?"
)
class MetadataGetColumnsResult extends DWSResult {
  @BeanProperty var columns: util.List[java.util.Map[String, Any]] = _

  def getAllColumns: util.List[MetaColumnInfo] = {
    import scala.collection.JavaConverters._
    columns.asScala
      .map(x => {
        val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
        DWSHttpClient.jacksonJson.readValue(str, classOf[MetaColumnInfo])
      })
      .asJava
  }

}
