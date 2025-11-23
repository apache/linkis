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

package org.apache.linkis.ujes.jdbc

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.ujes.jdbc.UJESSQLDriverMain._

import org.apache.commons.lang3.StringUtils

import java.sql.{
  Connection,
  Driver,
  DriverManager,
  DriverPropertyInfo,
  SQLFeatureNotSupportedException
}
import java.util.{Locale, Properties}
import java.util.logging.Logger

import scala.collection.JavaConverters._

class UJESSQLDriverMain extends Driver with Logging {

  override def connect(url: String, properties: Properties): Connection = if (acceptsURL(url)) {
    val props = if (properties != null) properties else new Properties
    // The putting is performed iteratively in order to avoid this error (Java > 8):
    // [error] both method putAll in class Properties of type (x$1: java.util.Map[_, _])Unit
    // [error] and  method putAll in class Hashtable of type (x$1: java.util.Map[_ <: Object, _ <: Object])Unit
    parseURL(url).asScala.foreach { case (key, value) =>
      props.put(key, value)
    }
    logger.info(s"input url:$url, properties:$properties")
    val ujesClient = UJESClientFactory.getUJESClient(props)
    new LinkisSQLConnection(ujesClient, props)
  } else {
    null
  }

  override def acceptsURL(url: String): Boolean = url.startsWith(URL_PREFIX)

  private def parseURL(url: String): Properties = {
    val props = new Properties
    // add an entry to get url
    props.setProperty("URL", url)
    url match {
      case URL_REGEX(host, port, db, params) =>
        if (StringUtils.isNotBlank(host)) props.setProperty(HOST, host)
        if (StringUtils.isNotBlank(port)) props.setProperty(PORT, port.substring(1))
        if (StringUtils.isNotBlank(db) && db.length > 1) props.setProperty(DB_NAME, db.substring(1))
        if (StringUtils.isNotBlank(params) && params.length > 1) {
          val _params = params.substring(1)
          val kvs = _params.split(PARAM_SPLIT).map(_.split(KV_SPLIT)).filter {
            case Array(USER, value) =>
              props.setProperty(USER, value)
              false
            case Array(PASSWORD, value) =>
              props.setProperty(PASSWORD, value)
              false
            case Array(TOKEN_KEY, value) =>
              props.setProperty(TOKEN_KEY, value)
              false
            case Array(TOKEN_VALUE, value) =>
              props.setProperty(TOKEN_VALUE, value)
              false
            case Array(FIXED_SESSION, value) =>
              props.setProperty(FIXED_SESSION, value)
              false
            case Array(USE_SSL, value) =>
              props.setProperty(USE_SSL, value)
              false
            case Array(ENABLE_MULTI_RESULT, value) =>
              props.setProperty(ENABLE_MULTI_RESULT, value)
              false
            case Array(key, _) =>
              if (StringUtils.isBlank(key)) {
                throw new LinkisSQLException(
                  LinkisSQLErrorCode.BAD_URL,
                  "bad url for params: " + url
                )
              } else true
            case _ =>
              throw new LinkisSQLException(LinkisSQLErrorCode.BAD_URL, "bad url for params: " + url)
          }
          props.setProperty(PARAMS, kvs.map(_.mkString(KV_SPLIT)).mkString(PARAM_SPLIT))
        }
      case _ => throw new LinkisSQLException(LinkisSQLErrorCode.BAD_URL, "bad url: " + url)
    }
    props
  }

  override def getPropertyInfo(url: String, info: Properties): Array[DriverPropertyInfo] = {
    val props = if (info != null) info else new Properties
    // The putting is performed iteratively in order to avoid this error (Java > 8):
    // [error] both method putAll in class Properties of type (x$1: java.util.Map[_, _])Unit
    // [error] and  method putAll in class Hashtable of type (x$1: java.util.Map[_ <: Object, _ <: Object])Unit
    parseURL(url).asScala.foreach { case (key, value) =>
      props.put(key, value)
    }
    val hostProp = new DriverPropertyInfo(HOST, props.getProperty(HOST))
    hostProp.required = true
    val portProp = new DriverPropertyInfo(PORT, props.getProperty(PORT))
    portProp.required = false
    val userProp = new DriverPropertyInfo(USER, props.getProperty(USER))
    userProp.required = true
    val passwordProp = new DriverPropertyInfo(PASSWORD, props.getProperty(PASSWORD))
    passwordProp.required = true
    val dbName = new DriverPropertyInfo(DB_NAME, props.getProperty(DB_NAME))
    dbName.required = false
    val paramProp = new DriverPropertyInfo(PARAMS, props.getProperty(PARAMS))
    paramProp.required = false
    Array(hostProp, portProp, userProp, passwordProp, dbName, paramProp)
  }

  override def getMajorVersion: Int = DEFAULT_VERSION

  override def getMinorVersion: Int = 0

  override def jdbcCompliant(): Boolean = false

  override def getParentLogger: Logger = throw new SQLFeatureNotSupportedException(
    "Method not supported"
  )

}

object UJESSQLDriverMain {
  DriverManager.registerDriver(new UJESSQLDriverMain)
  private val URL_PREFIX = UJESSQLDriver.URL_PREFIX
  private val URL_REGEX = UJESSQLDriver.URL_REGEX.r

  val HOST = UJESSQLDriver.HOST
  val PORT = UJESSQLDriver.PORT
  val DB_NAME = UJESSQLDriver.DB_NAME
  val PARAMS = UJESSQLDriver.PARAMS

  val USER = UJESSQLDriver.USER
  val TOKEN_KEY = UJESSQLDriver.TOKEN_KEY
  val TOKEN_VALUE = UJESSQLDriver.TOKEN_VALUE
  val PASSWORD = UJESSQLDriver.PASSWORD
  val TABLEAU_SERVER = UJESSQLDriver.TABLEAU_SERVER
  val FIXED_SESSION = UJESSQLDriver.FIXED_SESSION
  val ENABLE_MULTI_RESULT = UJESSQLDriver.ENABLE_MULTI_RESULT

  val USE_SSL = UJESSQLDriver.USE_SSL

  val VERSION = UJESSQLDriver.VERSION
  val DEFAULT_VERSION = UJESSQLDriver.DEFAULT_VERSION
  val MAX_CONNECTION_SIZE = UJESSQLDriver.MAX_CONNECTION_SIZE
  val READ_TIMEOUT = UJESSQLDriver.READ_TIMEOUT
  val ENABLE_DISCOVERY = UJESSQLDriver.ENABLE_DISCOVERY
  val ENABLE_LOADBALANCER = UJESSQLDriver.ENABLE_LOADBALANCER
  val CREATOR = UJESSQLDriver.CREATOR

  val TABLEAU = UJESSQLDriver.TABLEAU

  val VARIABLE_HEADER = UJESSQLDriver.VARIABLE_HEADER

  def getConnectionParams(
      connectionParams: String,
      variableMap: java.util.Map[String, Any]
  ): String = {
    val variables = variableMap.asScala
      .map(kv => VARIABLE_HEADER + kv._1 + KV_SPLIT + kv._2)
      .mkString(PARAM_SPLIT)
    if (StringUtils.isNotBlank(connectionParams)) connectionParams + PARAM_SPLIT + variables
    else variables
  }

  def getConnectionParams(version: String, creator: String): String =
    getConnectionParams(version, creator, 10, 45000)

  def getConnectionParams(
      version: String,
      creator: String,
      maxConnectionSize: Int,
      readTimeout: Long
  ): String =
    getConnectionParams(version, creator, maxConnectionSize, readTimeout, false, false)

  def getConnectionParams(
      version: String,
      creator: String,
      maxConnectionSize: Int,
      readTimeout: Long,
      enableDiscovery: Boolean,
      enableLoadBalancer: Boolean
  ): String = {
    val sb = new StringBuilder
    if (StringUtils.isNotBlank(version)) sb.append(VERSION).append(KV_SPLIT).append(version)
    if (maxConnectionSize > 0) {
      sb.append(PARAM_SPLIT).append(MAX_CONNECTION_SIZE).append(KV_SPLIT).append(maxConnectionSize)
    }
    if (readTimeout > 0) {
      sb.append(PARAM_SPLIT).append(READ_TIMEOUT).append(KV_SPLIT).append(readTimeout)
    }
    if (enableDiscovery) {
      sb.append(PARAM_SPLIT).append(ENABLE_DISCOVERY).append(KV_SPLIT).append(enableDiscovery)
      if (enableLoadBalancer) {
        sb.append(PARAM_SPLIT)
          .append(ENABLE_LOADBALANCER)
          .append(KV_SPLIT)
          .append(enableLoadBalancer)
      }
    }
    if (sb.startsWith(PARAM_SPLIT)) sb.toString.substring(PARAM_SPLIT.length) else sb.toString
  }

  private[jdbc] val PARAM_SPLIT = UJESSQLDriver.PARAM_SPLIT
  private[jdbc] val KV_SPLIT = UJESSQLDriver.KV_SPLIT

  def main(args: Array[String]): Unit = {}
}
