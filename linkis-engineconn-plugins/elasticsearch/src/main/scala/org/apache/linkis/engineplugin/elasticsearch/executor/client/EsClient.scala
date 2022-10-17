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

package org.apache.linkis.engineplugin.elasticsearch.executor.client

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration._
import org.apache.linkis.server.JMap

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.apache.http.Header
import org.apache.http.auth.{AUTH, Credentials, UsernamePasswordCredentials}
import org.apache.http.message.BufferedHeader
import org.apache.http.util.{CharArrayBuffer, EncodingUtils}

import java.nio.charset.StandardCharsets.UTF_8
import java.util

import scala.collection.JavaConverters._

import org.elasticsearch.client.{Cancellable, Request, RequestOptions, ResponseListener, RestClient}
import org.elasticsearch.client.sniff.Sniffer

trait EsClientOperate {

  def execute(
      code: String,
      options: util.Map[String, String],
      responseListener: ResponseListener
  ): Cancellable

  def close(): Unit

}

abstract class EsClient(datasourceName: String, client: RestClient, sniffer: Sniffer)
    extends EsClientOperate {

  def getDatasourceName: String = datasourceName

  def getRestClient: RestClient = client

  def getSniffer: Sniffer = sniffer

  override def close(): Unit = Utils.tryQuietly {
    sniffer match {
      case s: Sniffer => s.close()
      case _ =>
    }
    client match {
      case c: RestClient => c.close()
      case _ =>
    }
  }

}

class EsClientImpl(datasourceName: String, client: RestClient, sniffer: Sniffer)
    extends EsClient(datasourceName, client, sniffer) {

  override def execute(
      code: String,
      options: util.Map[String, String],
      responseListener: ResponseListener
  ): Cancellable = {
    val request = createRequest(code, options)
    client.performRequestAsync(request, responseListener)
  }

  private def createRequest(code: String, options: util.Map[String, String]): Request = {
    val endpoint = ES_HTTP_ENDPOINT.getValue(options)
    val method = ES_HTTP_METHOD.getValue(options)
    val request = new Request(method, endpoint)
    request.setOptions(getRequestOptions(options))
    request.setJsonEntity(code)
    request
  }

  private def getRequestOptions(options: util.Map[String, String]): RequestOptions = {
    val builder = RequestOptions.DEFAULT.toBuilder()

    val username = ES_USERNAME.getValue(options)
    val password = ES_PASSWORD.getValue(options)
    // username / password convert to base auth
    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
      val authHeader =
        authenticate(new UsernamePasswordCredentials(username, password), UTF_8.name())
      builder.addHeader(authHeader.getName, authHeader.getValue)
    }

    options.asScala
      .filter(entry =>
        entry._1 != null && entry._2 != null && entry._1.startsWith(ES_HTTP_HEADER_PREFIX)
      )
      .foreach(entry => builder.addHeader(entry._1, entry._2))

    builder.build()
  }

  private def authenticate(credentials: Credentials, charset: String): Header = {
    val tmp = new StringBuilder
    tmp.append(credentials.getUserPrincipal.getName)
    tmp.append(":")
    tmp.append(
      if (credentials.getPassword == null) "null"
      else credentials.getPassword
    )
    val base64password = Base64.encodeBase64(EncodingUtils.getBytes(tmp.toString, charset), false)
    val buffer = new CharArrayBuffer(32)
    buffer.append(AUTH.WWW_AUTH_RESP)
    buffer.append(": Basic ")
    buffer.append(base64password, 0, base64password.length)
    new BufferedHeader(buffer)
  }

}
