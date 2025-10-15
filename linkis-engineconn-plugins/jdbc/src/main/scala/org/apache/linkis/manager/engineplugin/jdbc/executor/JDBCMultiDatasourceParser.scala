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

package org.apache.linkis.manager.engineplugin.jdbc.executor

import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient
import org.apache.linkis.datasource.client.request.{
  GetInfoPublishedByDataSourceNameAction,
  GetInfoPublishedByUserIpPortAction
}
import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType
import org.apache.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration.{
  CHANGE_DS_TYPE_TO_MYSQL,
  DS_TYPES_TO_EXECUTE_TASK_BY_JDBC
}
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant
import org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary._
import org.apache.linkis.manager.engineplugin.jdbc.exception.{
  JDBCGetDatasourceInfoException,
  JDBCParamsIllegalException
}

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._

object JDBCMultiDatasourceParser extends Logging {

  private val MYSQL_SQL_CONNECT_URL = "jdbc:mysql://%s:%s/%s"
  private val ORACLE_SQL_CONNECT_URL = "jdbc:oracle:thin:@%s:%s:%s"
  private val POSTGRESQL_SQL_CONNECT_URL = "jdbc:postgresql://%s:%s/%s"

  def queryDatasourceInfoByName(
      datasourceName: String,
      username: String,
      system: String
  ): util.Map[String, String] = {
    logger.info(s"Starting query [$system, $username, $datasourceName] datasource info ......")
    val dataSourceClient = new LinkisDataSourceRemoteClient()
    var dataSource: DataSource = null

    dataSource = dataSourceClient
      .getInfoPublishedByDataSourceName(
        GetInfoPublishedByDataSourceNameAction
          .builder()
          .setSystem(system)
          .setDataSourceName(datasourceName)
          .setUser(username)
          .build()
      )
      .getDataSource

    queryDatasourceInfo(datasourceName, dataSource)
  }

  def queryDatasourceInfo(
      datasourceName: String,
      dataSource: DataSource
  ): util.Map[String, String] = {
    val dsConnInfo = new util.HashMap[String, String]()

    if (strObjIsBlank(dataSource)) {
      throw JDBCParamsIllegalException(
        DATA_SOURCE_INFO_NOT_FOUND.getErrorCode,
        MessageFormat.format(DATA_SOURCE_INFO_NOT_FOUND.getErrorDesc, datasourceName)
      )
    }

    if (dataSource.getPublishedVersionId == null || dataSource.getPublishedVersionId <= 0) {
      throw JDBCParamsIllegalException(
        DATA_SOURCE_NOT_PUBLISHED.getErrorCode,
        MessageFormat.format(DATA_SOURCE_NOT_PUBLISHED.getErrorDesc, datasourceName)
      )
    }

    var maxVersionId = "0"
    if (dataSource.getPublishedVersionId != null) {
      maxVersionId = dataSource.getPublishedVersionId.toString
    }
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_MAX_VERSION_ID, maxVersionId)

    if (dataSource.isExpire) {
      throw JDBCParamsIllegalException(
        DATA_SOURCE_EXPIRED.getErrorCode,
        MessageFormat.format(DATA_SOURCE_EXPIRED.getErrorDesc, datasourceName)
      )
    }

    if (
        dataSource.getDataSourceType == null || StringUtils.isBlank(
          dataSource.getDataSourceType.getName
        )
    ) {
      throw JDBCParamsIllegalException(
        DATA_SOURCE_JDBC_TYPE_NOT_NULL.getErrorCode,
        DATA_SOURCE_JDBC_TYPE_NOT_NULL.getErrorDesc
      )
    }

    var dbType = dataSource.getDataSourceType.getName
    val dbConnParams = dataSource.getConnectParams
    if (dbConnParams == null || dbConnParams.isEmpty) {
      throw JDBCParamsIllegalException(
        JDBC_CONNECTION_INFO_NOT_NULL.getErrorCode,
        JDBC_CONNECTION_INFO_NOT_NULL.getErrorDesc
      )
    }

    val driverClassName = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_DRIVER)
    if (strObjIsBlank(driverClassName)) {
      throw JDBCParamsIllegalException(
        JDBC_DRIVER_CLASS_NAME_NOT_NULL.getErrorCode,
        JDBC_DRIVER_CLASS_NAME_NOT_NULL.getErrorDesc
      )
    }

    // check dbType
    if (!DS_TYPES_TO_EXECUTE_TASK_BY_JDBC.contains(dbType)) {
      throw new JDBCGetDatasourceInfoException(
        UNSUPPORTED_DS_TYPE.getErrorCode,
        MessageFormat.format(UNSUPPORTED_DS_TYPE.getErrorDesc, dbType)
      )
    }
    if (CHANGE_DS_TYPE_TO_MYSQL.contains(dbType)) {
      dbType = "mysql"
    }

    val jdbcUrl = createJdbcUrl(dbType, dbConnParams)
    logger.info(s"The url parsed from the data source connection information is $jdbcUrl")
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_URL, jdbcUrl)
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_DRIVER, driverClassName.toString)
    appendJdbcAuthType(dbConnParams, dsConnInfo)
  }

  def createJdbcUrl(dbType: String, dbConnParams: util.Map[String, Object]): String = {
    val host = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_HOST)
    if (strObjIsBlank(host)) {
      throw JDBCParamsIllegalException(
        JDBC_HOST_NOT_NULL.getErrorCode,
        JDBC_HOST_NOT_NULL.getErrorDesc
      )
    }
    val port = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PORT)
    if (strObjIsBlank(port)) {
      throw JDBCParamsIllegalException(
        JDBC_PORT_NOT_NULL.getErrorCode,
        JDBC_PORT_NOT_NULL.getErrorDesc
      )
    }
    var jdbcUrl = s"jdbc:$dbType://$host:$port"
    val dbName = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_DB_NAME)
    dbType match {
      case "oracle" =>
        val instance: Object = dbConnParams.get("instance")
        jdbcUrl = String.format(ORACLE_SQL_CONNECT_URL, host, port, instance)
      case "postgresql" =>
        var instance: Object = dbConnParams.get("instance")
        if (strObjIsBlank(instance) && strObjIsNotBlank(dbName)) {
          instance = dbName
        }
        jdbcUrl = String.format(POSTGRESQL_SQL_CONNECT_URL, host, port, instance)
      case _ =>
        jdbcUrl = s"jdbc:$dbType://$host:$port"
        if (strObjIsNotBlank(dbName)) {
          jdbcUrl = s"$jdbcUrl/$dbName"
        }
    }
    logger.info(s"jdbc ${dbType} connection_url: $jdbcUrl")

    val params = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PARAMS)
    val paramsMap =
      if (strObjIsNotBlank(params)) convertJsonStrToMap(params.toString)
      else new util.HashMap[String, Object]()

    if (!paramsMap.isEmpty) {
      val headConf = paramsMap.asScala.head
      jdbcUrl = s"$jdbcUrl?${headConf._1}=${headConf._2}"
      paramsMap.remove(headConf._1)
    }

    if (!paramsMap.isEmpty) {
      val paramsJoin =
        for ((k, v) <- paramsMap.asScala) yield s"$k=${v.toString}".toList.mkString("&")
      jdbcUrl = s"$jdbcUrl&$paramsJoin"
    }

    jdbcUrl
  }

  def appendJdbcAuthType(
      dbConnParams: util.Map[String, Object],
      dsConnInfo: util.HashMap[String, String]
  ): util.HashMap[String, String] = {
    val username = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_USERNAME)
    val password = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_PASSWORD)
    val enableKerberos = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS)
    val kerberosPrincipal = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PRINCIPAL)
    val kerberosKeytab = dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_KEYTAB)
    var authType: JdbcAuthType = JdbcAuthType.SIMPLE
    if (strObjIsNotBlank(username) && strObjIsNotBlank(password)) {
      authType = JdbcAuthType.USERNAME
    } else {
      if (strObjIsNotBlank(enableKerberos) && enableKerberos.toString.toBoolean) {
        authType = JdbcAuthType.KERBEROS
        if (strObjIsBlank(kerberosPrincipal)) {
          throw JDBCParamsIllegalException(
            KERBEROS_PRINCIPAL_NOT_NULL.getErrorCode,
            KERBEROS_PRINCIPAL_NOT_NULL.getErrorDesc
          )
        }
        if (strObjIsBlank(kerberosKeytab)) {
          throw JDBCParamsIllegalException(
            KERBEROS_KEYTAB_NOT_NULL.getErrorCode,
            KERBEROS_KEYTAB_NOT_NULL.getErrorDesc
          )
        }
      } else {
        authType = JdbcAuthType.SIMPLE
      }
    }

    authType match {
      case JdbcAuthType.SIMPLE =>
        logger.info("jdbc simple auth type.")
      case JdbcAuthType.USERNAME =>
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_USERNAME, username.toString)
        dsConnInfo.put(JDBCEngineConnConstant.JDBC_PASSWORD, password.toString)
      case JdbcAuthType.KERBEROS =>
        dsConnInfo.put(
          JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL,
          kerberosPrincipal.toString
        )
        dsConnInfo.put(
          JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION,
          kerberosKeytab.toString
        )
        val enableKerberosProxyUser =
          dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS_PROXY_USER)
        if (strObjIsNotBlank(enableKerberosProxyUser)) {
          dsConnInfo.put(
            JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_PROXY_ENABLE,
            enableKerberosProxyUser.toString
          )
        }
        val kerberosProxyUserProperty =
          dbConnParams.get(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PROXY_USER_PROPERTY)
        if (strObjIsNotBlank(kerberosProxyUserProperty)) {
          dsConnInfo.put(
            JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY,
            kerberosProxyUserProperty.toString
          )
        }
      case _ =>
        throw JDBCParamsIllegalException(
          UNSUPPORTED_AUTHENTICATION_TYPE.getErrorCode,
          MessageFormat.format(UNSUPPORTED_AUTHENTICATION_TYPE.getErrorDesc, authType.getAuthType)
        )

    }
    dsConnInfo.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, authType.getAuthType)
    dsConnInfo
  }

  private def convertJsonStrToMap(jsonStr: String): util.Map[String, Object] = {
    JsonUtils.jackson.readValue(jsonStr, classOf[util.Map[String, Object]])
  }

  private def strObjIsNotBlank(str: Object): Boolean = {
    str != null && StringUtils.isNotBlank(str.toString)
  }

  private def strObjIsBlank(str: Object): Boolean = {
    !strObjIsNotBlank(str)
  }

  def queryDatasourceInfoByConnParams(
      createUser: String,
      proxyUser: String,
      ip: String,
      port: String,
      datasourceTypeName: String
  ): util.Map[String, String] = {
    val dataSourceClient = new LinkisDataSourceRemoteClient()
    val action: GetInfoPublishedByUserIpPortAction = GetInfoPublishedByUserIpPortAction.builder
      .setDatasourceTypeName(datasourceTypeName)
      .setUser(createUser)
      .setDatasourceUser(proxyUser)
      .setIp(ip)
      .setPort(port)
      .build // ignore parameter 'system'

    val dataSource: DataSource = dataSourceClient.getInfoPublishedByIpPort(action).getDataSource
    if (dataSource != null) {
      queryDatasourceInfo(dataSource.getDataSourceName, dataSource)
    } else {
      null
    }

  }

}
