/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.jdbc.executer

import java.util
import org.apache.commons.lang.StringUtils
import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient
import org.apache.linkis.datasource.client.request.GetInfoByDataSourceNameAction
import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant
import org.apache.linkis.manager.engineplugin.jdbc.exception.JDBCParamsIllegalException
import scala.collection.JavaConversions._

object JDBCMultiDatasourceParser extends Logging {

  def queryDatasourceInfoByName(datasourceName: String, username: String): util.Map[String, String] = {
    info(s"Starting query [$username, $datasourceName] datasource info ......")
    val dataSourceClient = new LinkisDataSourceRemoteClient()
    var dataSource: DataSource = null
    Utils.tryCatch {
      dataSource = dataSourceClient.getInfoByDataSourceName(GetInfoByDataSourceNameAction.builder()
        .setSystem("")
        .setDataSourceName(datasourceName)
        .setUser(username)
        .build()).getDataSource
    } {
      case e: Exception => warn(s"Get data source info error, $e")
    }
    queryDatasourceInfo(datasourceName, dataSource)
  }

  def queryDatasourceInfo(datasourceName: String, dataSource: DataSource): util.Map[String, String] = {
    val dsConnInfo = new util.HashMap[String, String]()

    if (dataSource == null) {
      throw JDBCParamsIllegalException(s"Data source [$datasourceName] info not found!")
    }

    if (dataSource.getPublishedVersionId == null || dataSource.getPublishedVersionId <= 0) {
      throw JDBCParamsIllegalException(s"Data source [$datasourceName] not yet published!")
    }

    if (dataSource.isExpire) {
      throw JDBCParamsIllegalException(s"Data source [$datasourceName] is expired!")
    }

    if (dataSource.getDataSourceType == null || StringUtils.isBlank(dataSource.getDataSourceType.getName)) {
      throw JDBCParamsIllegalException("The data source jdbc type cannot be null!")
    }

    val dbType = dataSource.getDataSourceType.getName
    val dbConnParams = dataSource.getConnectParams
    if (dbConnParams == null || dbConnParams.isEmpty) {
      throw JDBCParamsIllegalException("The data source jdbc connection info cannot be null!")
    }

    val driverClassName = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_DRIVER)
    if (driverClassName == null || StringUtils.isBlank(driverClassName.toString)) {
      throw JDBCParamsIllegalException("The data source jdbc driverClassName cannot be null!")
    }

    val jdbcUrl = createJdbcUrl(dbType, dbConnParams)
    info(s"The url parsed from the data source connection information is $jdbcUrl")
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_URL, jdbcUrl)
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_DRIVER, driverClassName.toString)
    appendJdbcAuthType(dbConnParams, dsConnInfo)
  }

  def createJdbcUrl(dbType: String, dbConnParams: util.Map[String, Object]): String = {
    val host = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_HOST)
    if (host == null || StringUtils.isBlank(host.toString)) {
      throw JDBCParamsIllegalException("The data source jdbc connection host cannot be null!")
    }
    val port = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PORT)
    if (port == null || StringUtils.isBlank(port.toString)) {
      throw JDBCParamsIllegalException("The data source jdbc connection port cannot be null!")
    }
    var jdbcUrl = s"jdbc:$dbType://$host:$port"
    val dbName = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_DB_NAME)
    if (dbName != null && StringUtils.isNotBlank(dbName.toString)) {
      jdbcUrl = s"$jdbcUrl/$dbName"
    }

    val params = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PARAMS)
    var paramsMap: util.Map[String, Object] = new util.HashMap[String, Object]()
    if (params != null && StringUtils.isNotBlank(params.toString)) {
      paramsMap = JsonUtils.jackson.readValue(params.toString, classOf[util.Map[String, Object]])
    }

    if (!paramsMap.isEmpty) {
      val headConf = paramsMap.head
      jdbcUrl = s"$jdbcUrl?${headConf._1}=${headConf._2}"
      paramsMap.remove(headConf._1)
    }

    if (!paramsMap.isEmpty) {
      val paramsJoin = for ((k, v) <- paramsMap) yield s"$k=${v.toString}".toList.mkString("&")
      jdbcUrl = s"$jdbcUrl&$paramsJoin"
    }

    jdbcUrl
  }

  def appendJdbcAuthType(dbConnParams: util.Map[String, Object], dsConnInfo: util.HashMap[String, String]): util.HashMap[String, String] = {
    val username = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_USERNAME)
    val password = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PASSWORD)
    val enableKerberos = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS)
    val kerberosPrincipal = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PRINCIPAL)
    val kerberosKeytab = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_KEYTAB)
    var authType: JdbcAuthType = JdbcAuthType.SIMPLE
    if (username != null && StringUtils.isNotBlank(username.toString)
      && password != null && StringUtils.isNotBlank(password.toString)) {
      authType = JdbcAuthType.USERNAME
    } else {
      if (enableKerberos != null && enableKerberos.toString.toBoolean) {
        authType = JdbcAuthType.KERBEROS
        if (kerberosPrincipal == null || StringUtils.isBlank(kerberosPrincipal.toString)) {
          throw JDBCParamsIllegalException("In the jdbc authentication mode of kerberos, the kerberos principal cannot be empty!")
        }
        if (kerberosKeytab == null || StringUtils.isBlank(kerberosKeytab.toString)) {
          throw JDBCParamsIllegalException("In the jdbc authentication mode of kerberos, the kerberos keytab cannot be empty!")
        }
      } else {
        authType = JdbcAuthType.SIMPLE
      }
    }

    authType match {
      case JdbcAuthType.SIMPLE =>
        info("jdbc simple auth type.")
      case JdbcAuthType.USERNAME =>
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_USERNAME, username.toString)
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_PASSWORD, password.toString)
      case JdbcAuthType.KERBEROS =>
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL, kerberosPrincipal.toString)
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION, kerberosKeytab.toString)
        val enableKerberosProxyUser = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS_PROXY_USER)
        if (enableKerberosProxyUser != null && StringUtils.isNotBlank(enableKerberosProxyUser.toString)) {
          dsConnInfo.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_PROXY_ENABLE, enableKerberosProxyUser.toString)
        }
        val kerberosProxyUserProperty = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PROXY_USER_PROPERTY)
        if (kerberosProxyUserProperty != null && StringUtils.isNotBlank(kerberosProxyUserProperty.toString)) {
          dsConnInfo.put(JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, kerberosProxyUserProperty.toString)
        }
      case _ => throw JDBCParamsIllegalException(s"Unsupported authentication type ${authType.getAuthType}")
    }
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, authType.getAuthType)
    dsConnInfo
  }
}
