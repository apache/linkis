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

package org.apache.linkis.errorcode.client.action

import org.apache.linkis.errorcode.common.CommonConf
import org.apache.linkis.httpclient.request.{GetAction, POSTAction, UserAction}

import java.lang
import java.lang.reflect.Type

import com.google.gson.{
  GsonBuilder,
  JsonElement,
  JsonPrimitive,
  JsonSerializationContext,
  JsonSerializer
}

trait ErrorCodeAction extends UserAction {
  private var user: String = "hadoop"

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user

  implicit val gson = new GsonBuilder()
    .setPrettyPrinting()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .serializeNulls
    .registerTypeAdapter(
      classOf[java.lang.Double],
      new JsonSerializer[java.lang.Double] {

        override def serialize(
            t: lang.Double,
            `type`: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement =
          if (t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)

      }
    )
    .create

  val urlPrefix: String = "/api/rest_j/v1/errorcode/"
  val getAllUrlSuffix: String = CommonConf.GET_ERRORCODE_URL

  val getAllUrl: String = urlPrefix + getAllUrlSuffix

}

abstract class ErrorCodeGetAction extends GetAction with ErrorCodeAction

abstract class ContextPostAction extends POSTAction with ErrorCodeAction {
  override def getRequestPayload: String = gson.toJson(getRequestPayloads)
}

case class ErrorCodeGetAllAction() extends ErrorCodeGetAction {
  override def getURL: String = getAllUrl
}
