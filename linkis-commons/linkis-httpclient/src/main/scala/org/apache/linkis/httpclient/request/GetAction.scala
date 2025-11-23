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

package org.apache.linkis.httpclient.request

import org.apache.linkis.common.conf.Configuration

import java.net.URLEncoder
import java.util

import scala.collection.JavaConverters._

abstract class GetAction extends HttpAction {
  private val queryParams: util.Map[String, Any] = new util.HashMap[String, Any]

  def setParameter(key: String, value: Any): Unit = this.queryParams.put(key, value)

  def getParameters: util.Map[String, Any] = queryParams

  override def getRequestBody: String = {
    val queryString = new StringBuilder
    queryParams.asScala.retain((k, v) => v != null && k != null).foreach { case (k, v) =>
      queryString
        .append(URLEncoder.encode(k, Configuration.BDP_ENCODING.getValue))
        .append("=")
        .append(URLEncoder.encode(v.toString, Configuration.BDP_ENCODING.getValue))
        .append("&")
    }
    if (!queryParams.isEmpty) queryString.deleteCharAt(queryString.length - 1)
    queryString.toString
  }

}
