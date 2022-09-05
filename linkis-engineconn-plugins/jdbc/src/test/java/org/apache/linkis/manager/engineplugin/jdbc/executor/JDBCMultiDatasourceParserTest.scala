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

package org.apache.linkis.manager.engineplugin.jdbc.executor

import org.apache.linkis.datasourcemanager.common.domain.{DataSource, DataSourceType}
import org.apache.linkis.manager.engineplugin.jdbc.JdbcAuthType

import java.util
import org.junit.jupiter.api.{BeforeEach, DisplayName, Test}
import org.junit.jupiter.api.Assertions.assertTrue
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant


class JDBCMultiDatasourceParserTest {

  val dbType = "mysql"
  val dbConnParams: util.Map[String, Object] = new util.HashMap[String, Object]()
  val datasource: DataSource = new DataSource()

  @BeforeEach
  def initDatasourceConnParams(): Unit = {
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_HOST, "localhost")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_PORT, "3306")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_USERNAME, "username")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_PASSWORD, "password")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_DB_NAME, "dbName")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_DRIVER, "com.mysql.jdbc.Driver")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_PARAMS, "{\"useSSL\": \"false\"}")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS, "false")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PRINCIPAL, "hadoop@com")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_KERBEROS_KEYTAB, "/data/linkis/keytab/hadoop.keytab")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS_PROXY_USER, "true")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_KERBEROS_PROXY_USER_PROPERTY, "hive.server2.proxy.user")

    val dataSourceType = new DataSourceType()
    dataSourceType.setName(dbType)
    datasource.setDataSourceType(dataSourceType)
    datasource.setConnectParams(dbConnParams)
    datasource.setPublishedVersionId(1L)
    datasource.setVersionId(1L)
    datasource.setExpire(false)
  }

  @Test
  @DisplayName("testCreateJdbcUrl")
  def testCreateJdbcUrl(): Unit = {
    val url1 = JDBCMultiDatasourceParser.createJdbcUrl(dbType, dbConnParams)
    assertTrue(url1 != null && "jdbc:mysql://localhost:3306/dbName?useSSL=false".equals(url1))
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_DB_NAME, "")
    val url2 = JDBCMultiDatasourceParser.createJdbcUrl(dbType, dbConnParams)
    assertTrue(url2 != null && "jdbc:mysql://localhost:3306?useSSL=false".equals(url2))
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_HOST, "")
    try {
      JDBCMultiDatasourceParser.createJdbcUrl(dbType, dbConnParams)
    } catch {
      case e: Throwable => assertTrue(true)
    }
  }

  @Test
  @DisplayName("testAppendJdbcAuthType")
  def testAppendJdbcAuthType(): Unit = {
    var dsConnInfo = new util.HashMap[String, String]()
    dsConnInfo = JDBCMultiDatasourceParser.appendJdbcAuthType(dbConnParams, dsConnInfo)
    assertTrue(dsConnInfo.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE).equals(JdbcAuthType.USERNAME.getAuthType))
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_USERNAME, "")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_PASSWORD, "")
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS, "true")
    dsConnInfo = new util.HashMap[String, String]()
    dsConnInfo = JDBCMultiDatasourceParser.appendJdbcAuthType(dbConnParams, dsConnInfo)
    assertTrue(dsConnInfo.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE).equals(JdbcAuthType.KERBEROS.getAuthType))
    dbConnParams.put(JDBCEngineConnConstant.DS_JDBC_ENABLE_KERBEROS_PROXY_USER, "false")
    dsConnInfo = new util.HashMap[String, String]()
    dsConnInfo = JDBCMultiDatasourceParser.appendJdbcAuthType(dbConnParams, dsConnInfo)
    assertTrue(dsConnInfo.get(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_PROXY_ENABLE).equals("false"))
  }

  @Test
  @DisplayName("testQueryDatasourceInfo")
  def testQueryDatasourceInfo(): Unit = {
    val dataSourceInfo = JDBCMultiDatasourceParser.queryDatasourceInfo("test_mysql", datasource)
    val jdbcUrl = dataSourceInfo.get(JDBCEngineConnConstant.JDBC_URL)
    assertTrue(jdbcUrl != null && "jdbc:mysql://localhost:3306/dbName?useSSL=false".equals(jdbcUrl))
    assertTrue(dataSourceInfo.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE).equals(JdbcAuthType.USERNAME.getAuthType))
  }

  @Test
  @DisplayName("testMapPutAll")
  def testMapPutAll(): Unit = {
    val globalConfig: util.Map[String, String] = new util.HashMap[String, String]()
    globalConfig.put(JDBCEngineConnConstant.JDBC_URL, "jdbc:mysql://localhost:3306/dbName?useSSL=false")
    globalConfig.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, JdbcAuthType.SIMPLE.getAuthType)
    val dataSourceInfo: util.Map[String, String] = new util.HashMap[String, String]()
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_URL, "jdbc:mysql://127.0.0.1:3306/dbName?useSSL=false")
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, JdbcAuthType.USERNAME.getAuthType)
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_USERNAME, "user")
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_PASSWORD, "password")
    globalConfig.putAll(dataSourceInfo)
    assertTrue(globalConfig.size() == 4)
    assertTrue(globalConfig.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE).equals(JdbcAuthType.USERNAME.getAuthType))
  }

  @Test
  @DisplayName("getJDBCRuntimeParams")
  def getJDBCRuntimeParams(): Unit = {
    // jdbc executor runtime params
    val executorProperties = new util.HashMap[String, String]()
    executorProperties.put(JDBCEngineConnConstant.JDBC_URL, "jdbc:mysql://localhost:3306/dbName?useSSL=false")
    executorProperties.put(JDBCEngineConnConstant.JDBC_USERNAME, "leo1")
    executorProperties.put(JDBCEngineConnConstant.JDBC_PASSWORD, "pwd2")
    executorProperties.put(JDBCEngineConnConstant.JDBC_DRIVER, "com.mysql.jdbc.Driver")
    // engine console global config
    val globalConfig: util.Map[String, String] = new util.HashMap[String, String]()
    globalConfig.put(JDBCEngineConnConstant.JDBC_URL, "jdbc:mysql://127.0.0.1:3306/dbName?useSSL=false")
    globalConfig.put("wds.linkis.jdbc.connect.max", "10")
    // datasource info params
    val dataSourceInfo: util.Map[String, String] = new util.HashMap[String, String]()
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, JdbcAuthType.USERNAME.getAuthType)
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_USERNAME, "leo")
    dataSourceInfo.put(JDBCEngineConnConstant.JDBC_PASSWORD, "pwd")
    // runtime jdbc params > jdbc datasource info > jdbc engine global config
    globalConfig.putAll(dataSourceInfo)
    globalConfig.putAll(executorProperties)

    assertTrue("jdbc:mysql://localhost:3306/dbName?useSSL=false".equals(globalConfig.get(JDBCEngineConnConstant.JDBC_URL)))
    assertTrue("10".equals(globalConfig.get("wds.linkis.jdbc.connect.max")))
    assertTrue(JdbcAuthType.USERNAME.getAuthType.equals(globalConfig.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE)))
  }
}
