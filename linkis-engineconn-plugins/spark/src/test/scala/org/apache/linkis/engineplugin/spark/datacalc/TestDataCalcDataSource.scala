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

package org.apache.linkis.engineplugin.spark.datacalc

import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.engineplugin.spark.datacalc.service.LinkisDataSourceContext
import org.apache.linkis.server.BDPJettyServerHelper

import org.junit.jupiter.api.{Assertions, Test};

class TestDataCalcDataSource {

  @Test
  def testGetMySqlDataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(mysqlJson, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  @Test
  def testGetPostgreSqlDataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(postgresqlJson, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  @Test
  def testGetClickHouseDataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(clickhouseJson, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  @Test
  def testGetOracleDataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(oracleJson, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  @Test
  def testGetSqlServerDataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(sqlserverJson, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  @Test
  def testGetDB2DataSource: Unit = {
    val datasource: DataSource =
      BDPJettyServerHelper.jacksonJson.readValue(db2Json, classOf[DataSource])
    val context = new LinkisDataSourceContext(datasource)
    val source = context.getDataCalcDataSource()
    println(source)
    Assertions.assertNotNull(source)
  }

  val mysqlJson =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "databaseName": "test_db",
      |        "port": "37001",
      |        "host": "testdb-mysql.linkis.com",
      |        "driverClassName": "com.mysql.cj.jdbc.Driver",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "mysql",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

  val postgresqlJson =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "databaseName": "test_db",
      |        "port": "37001",
      |        "host": "testdb-postgresql.linkis.com",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "postgresql",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

  val clickhouseJson =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "databaseName": "test_db",
      |        "address": "server1,server2,server3",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "clickhouse",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

  val oracleJson =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "address": "testdb-oracle.linkis.com:5021",
      |        "serviceName":"test.linkis.com",
      |        "server":"server_test",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "oracle",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

  val sqlserverJson =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "databaseName": "test_db",
      |        "port": "37001",
      |        "host": "testdb-sqlserver.linkis.com",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "sqlserver",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

  val db2Json =
    """
      |{
      |    "id": 1,
      |    "dataSourceName": "test",
      |    "dataSourceDesc": "测试数据源",
      |    "dataSourceTypeId": 1,
      |    "createSystem": "Linkis",
      |    "connectParams": {
      |        "username": "test_db_rw",
      |        "password": "p@ssw0rd",
      |        "databaseName": "test_db",
      |        "port": "37001",
      |        "host": "testdb-db2.linkis.com",
      |        "params": "{\"params1\":\"value1\", \"params2\":\"value2\"}"
      |    },
      |    "createTime": 1663568239000,
      |    "modifyTime": 1670853368000,
      |    "modifyUser": "linkis",
      |    "createUser": "linkis",
      |    "versionId": 3,
      |    "expire": false,
      |    "dataSourceType": {
      |        "name": "db2",
      |        "layers": 0
      |    }
      |}
      |""".stripMargin

}
