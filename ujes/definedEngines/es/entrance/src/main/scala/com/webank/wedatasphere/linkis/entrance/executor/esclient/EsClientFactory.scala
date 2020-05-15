package com.webank.wedatasphere.linkis.entrance.executor.esclient

import java.util
import java.util.Map

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.entrance.conf.EsEntranceConfiguration._
import com.webank.wedatasphere.linkis.entrance.exception.EsParamsIllegalException
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils
import org.apache.http.{Header, HttpHost}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.sniff.Sniffer

import scala.collection.JavaConversions._

/**
 *
 * @author wang_zh
 * @date 2020/5/6
 */
object EsClientFactory {

  def getRestClient(options: JMap[String, String]): EsClient = {
    val key = getDatasourceName(options)
    if (StringUtils.isBlank(key)) {
      return defaultClient
    }

    if (!ES_CLIENT_MAP.contains(key)) {
      ES_CLIENT_MAP synchronized {
        if (!ES_CLIENT_MAP.contains(key)) {
          cacheClient(createRestClient(options))
        }
      }
    }

    ES_CLIENT_MAP.get(key)
  }

  private val MAX_CACHE_CLIENT_SIZE = 20
  private val ES_CLIENT_MAP: Map[String, EsClient] = new util.LinkedHashMap[String, EsClient]() {
    override def removeEldestEntry(eldest: Map.Entry[String, EsClient]): Boolean = if (size > MAX_CACHE_CLIENT_SIZE) {
      eldest.getValue.close()
      true
    } else {
      false
    }
  }

  private def getDatasourceName(options: JMap[String, String]): String = {
    options.getOrDefault(ES_DATASOURCE_NAME.key, "")
  }

  private def cacheClient(client: EsClient) = {
    ES_CLIENT_MAP.put(client.getDatasourceName, client)
  }

  private def createRestClient(options: JMap[String, String]): EsClient = {
    val clusterStr = options.get(ES_CLUSTER.key)
    if (StringUtils.isBlank(clusterStr)) {
      throw EsParamsIllegalException("cluster is blank!")
    }
    val cluster = getCluster(clusterStr)
    if (cluster.isEmpty) {
      throw EsParamsIllegalException("cluster is empty!")
    }
    val username = options.get(ES_USERNAME.key)
    val password = options.get(ES_PASSWORD.key)

    if (ES_AUTH_CACHE.getValue) {
      setAuthScope(cluster, username, password)
    }

    val httpHosts = cluster.map(item => new HttpHost(item._1, item._2))
    val builder = RestClient.builder(httpHosts: _*)
      .setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) => {
        if(!ES_AUTH_CACHE.getValue) {
          httpClientBuilder.disableAuthCaching
        }
//        httpClientBuilder.setDefaultRequestConfig(RequestConfig.DEFAULT)
//        httpClientBuilder.setDefaultConnectionConfig(ConnectionConfig.DEFAULT)
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)

      })
    if (defaultHeaders != null) {
      builder.setDefaultHeaders(defaultHeaders)
    }
    val client = builder.build

    val sniffer = if (ES_SNIFFER_ENABLE.getValue) {
      Sniffer.builder(client).build
    } else null

    val datasourceName = getDatasourceName(options)
    new EsClientImpl(datasourceName, client, sniffer)
  }

  // default cluster
  private val defaultCluster:Array[(String, Int)] = getCluster(ES_CLUSTER.getValue)

  private val credentialsProvider: CredentialsProvider = {
    val credentialsProvider = new BasicCredentialsProvider()
    setAuthScope(defaultCluster, ES_USERNAME.getValue, ES_PASSWORD.getValue)
    credentialsProvider
  }

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

  private val defaultHeaders: Array[Header] = CommonVars.properties.entrySet()
    .filter(entry => entry.getKey != null && entry.getValue != null && entry.getKey.toString.startsWith(ES_HTTP_HEADER_PREFIX))
    .map(entry => new BasicHeader(entry.getKey.toString, entry.getValue.toString)).toArray[Header]

  // host1:port1,host2:port2 -> [(host1,port1),(host2,port2)]
  private def getCluster(clusterStr: String): Array[(String, Int)] = if (StringUtils.isNotBlank(clusterStr)) {
    clusterStr.split(",")
      .map(value => {
        val arr = value.split(":")
        (arr(0), arr(1).toInt)
      })
  } else Array()

  // set cluster auth
  private def setAuthScope(cluster: Array[(String, Int)], username: String, password: String): Unit = if (cluster != null && !cluster.isEmpty
    && StringUtils.isNotBlank(username)
    && StringUtils.isNotBlank(password)) {
    cluster.foreach{
      case (host, port) => {
        credentialsProvider.setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME)
          , new UsernamePasswordCredentials(username, password))
      }
      case _ =>
    }
  }

}
