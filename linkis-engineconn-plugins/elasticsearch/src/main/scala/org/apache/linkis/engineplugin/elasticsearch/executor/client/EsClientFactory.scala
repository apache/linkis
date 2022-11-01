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

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration._
import org.apache.linkis.engineplugin.elasticsearch.errorcode.EasticsearchErrorCodeSummary.CLUSTER_IS_BLANK
import org.apache.linkis.engineplugin.elasticsearch.exception.EsParamsIllegalException

import org.apache.commons.lang3.StringUtils
import org.apache.http.{Header, HttpHost}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.message.BasicHeader

import java.util
import java.util.Map

import scala.collection.JavaConverters._

import org.elasticsearch.client.{RestClient, RestClientBuilder}
import org.elasticsearch.client.sniff.Sniffer

object EsClientFactory {

  def getRestClient(options: util.Map[String, String]): EsClient = {
    val key = getDatasourceName(options)
    if (StringUtils.isBlank(key)) {
      return defaultClient
    }

    if (!ES_CLIENT_MAP.containsKey(key)) {
      ES_CLIENT_MAP synchronized {
        if (!ES_CLIENT_MAP.containsKey(key)) {
          cacheClient(createRestClient(options))
        }
      }
    }

    ES_CLIENT_MAP.get(key)
  }

  private val MAX_CACHE_CLIENT_SIZE = 20

  private val ES_CLIENT_MAP: Map[String, EsClient] = new util.LinkedHashMap[String, EsClient]() {

    override def removeEldestEntry(eldest: Map.Entry[String, EsClient]): Boolean =
      if (size > MAX_CACHE_CLIENT_SIZE) {
        eldest.getValue.close()
        true
      } else {
        false
      }

  }

  private def getDatasourceName(options: util.Map[String, String]): String = {
    options.getOrDefault(ES_DATASOURCE_NAME.key, "")
  }

  private def cacheClient(client: EsClient) = {
    ES_CLIENT_MAP.put(client.getDatasourceName, client)
  }

  private def createRestClient(options: util.Map[String, String]): EsClient = {
    val clusterStr = options.get(ES_CLUSTER.key)
    if (StringUtils.isBlank(clusterStr)) {
      throw EsParamsIllegalException(CLUSTER_IS_BLANK.getErrorDesc)
    }
    val cluster = getCluster(clusterStr)
    if (cluster.isEmpty) {
      throw EsParamsIllegalException(CLUSTER_IS_BLANK.getErrorDesc)
    }
    val username = options.get(ES_USERNAME.key)
    val password = options.get(ES_PASSWORD.key)

    if (ES_AUTH_CACHE.getValue) {
      setAuthScope(cluster, username, password)
    }

    val httpHosts = cluster.map(item => new HttpHost(item._1, item._2))
    val builder = RestClient
      .builder(httpHosts: _*)
      .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
        override def customizeHttpClient(
            httpAsyncClientBuilder: HttpAsyncClientBuilder
        ): HttpAsyncClientBuilder = {
          if (!ES_AUTH_CACHE.getValue) {
            httpAsyncClientBuilder.disableAuthCaching
          }
          //        httpClientBuilder.setDefaultRequestConfig(RequestConfig.DEFAULT)
          //        httpClientBuilder.setDefaultConnectionConfig(ConnectionConfig.DEFAULT)
          httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
      })
    if (defaultHeaders != null) {
      builder.setDefaultHeaders(defaultHeaders)
    }
    val client = builder.build

    val sniffer = if (ES_SNIFFER_ENABLE.getValue(options)) {
      Sniffer.builder(client).build
    } else null

    val datasourceName = getDatasourceName(options)
    new EsClientImpl(datasourceName, client, sniffer)
  }

  private val credentialsProvider: CredentialsProvider = new BasicCredentialsProvider()

  private val defaultClient = {
    val cluster = ES_CLUSTER.getValue
    if (StringUtils.isBlank(cluster)) {
      null
    } else {
      val defaultOpts = new util.HashMap[String, String]()
      defaultOpts.put(ES_CLUSTER.key, cluster)
      defaultOpts.put(ES_DATASOURCE_NAME.key, ES_DATASOURCE_NAME.getValue)
      defaultOpts.put(ES_USERNAME.key, ES_USERNAME.getValue)
      defaultOpts.put(ES_PASSWORD.key, ES_PASSWORD.getValue)
      val client = createRestClient(defaultOpts)
      cacheClient(client)
      client
    }
  }

  private val defaultHeaders: Array[Header] = CommonVars.properties
    .entrySet()
    .asScala
    .filter(entry =>
      entry.getKey != null && entry.getValue != null && entry.getKey.toString
        .startsWith(ES_HTTP_HEADER_PREFIX)
    )
    .map(entry => new BasicHeader(entry.getKey.toString, entry.getValue.toString))
    .toArray[Header]

  // host1:port1,host2:port2 -> [(host1,port1),(host2,port2)]
  private def getCluster(clusterStr: String): Array[(String, Int)] =
    if (StringUtils.isNotBlank(clusterStr)) {
      clusterStr
        .split(",")
        .map(value => {
          val arr = value.replace("http://", "").split(":")
          (arr(0).trim, arr(1).trim.toInt)
        })
    } else Array()

  // set cluster auth
  private def setAuthScope(
      cluster: Array[(String, Int)],
      username: String,
      password: String
  ): Unit = if (
      cluster != null && !cluster.isEmpty
      && StringUtils.isNotBlank(username)
      && StringUtils.isNotBlank(password)
  ) {
    cluster.foreach {
      case (host, port) =>
        credentialsProvider.setCredentials(
          new AuthScope(host, port, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME),
          new UsernamePasswordCredentials(username, password)
        )
      case _ =>
    }
  }

}
