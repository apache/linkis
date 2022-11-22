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

import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.datacalc.model.{DataCalcArrayData, DataCalcGroupData}
import org.apache.linkis.engineplugin.spark.extension.SparkPreExecutionHook

import org.junit.jupiter.api.{Assertions, Test};

class TestDataCalcPlugins {

  @Test
  def testArrayBuild: Unit = {
    val data = DataCalcArrayData.getData(arrayConfigJson)
    Assertions.assertTrue(data != null)

    val array = DataCalcExecution.getPlugins(data)
    Assertions.assertTrue(array != null)
  }

  @Test
  def testGroupBuild: Unit = {
    val data = DataCalcGroupData.getData(groupConfigJson)
    Assertions.assertTrue(data != null)

    val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)
    Assertions.assertTrue(sources != null)
    Assertions.assertTrue(transforms != null)
    Assertions.assertTrue(sinks != null)
  }

  @Test
  def testGetRealCode: Unit = {

    var preCode = arrayConfigJson

    val hooks = SparkPreExecutionHook.getSparkPreExecutionHooks();
    hooks.foreach(hook => {
      preCode = hook.callPreExecutionHook(null, preCode)
    })
    Assertions.assertTrue(preCode != null)
  }

  val arrayConfigJson =
    """
      |{
      |    "plugins": [
      |        {
      |            "name": "jdbc",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "test1",
      |                "url": "jdbc:mysql://127.0.0.1:3306/dip_linkis?characterEncoding=UTF-8",
      |                "driver": "com.mysql.jdbc.Driver",
      |                "user": "root",
      |                "password": "123456",
      |                "query": "select * from dip_linkis.linkis_ps_udf_baseinfo",
      |                "options": {
      |                }
      |            }
      |        },
      |        {
      |            "name": "sql",
      |            "type": "transformation",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "sql": "select * from test1"
      |            }
      |        },
      |        {
      |            "name": "file",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "hdfs:///tmp/test_new",
      |                "partitionBy": ["create_user"],
      |                "saveMode": "overwrite",
      |                "serializer": "csv"
      |            }
      |        },
      |        {
      |            "name": "file",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "hdfs:///tmp/test_new_no_partition",
      |                "saveMode": "overwrite",
      |                "serializer": "csv"
      |            }
      |        },
      |        {
      |            "name": "file",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "hdfs:///tmp/test_new_partition",
      |                "partitionBy": ["create_user"],
      |                "saveMode": "overwrite",
      |                "serializer": "csv"
      |            }
      |        },
      |        {
      |            "name": "file",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "test2",
      |                "path": "hdfs:///tmp/test_new_no_partition",
      |                "serializer": "csv",
      |                "columnNames": ["id", "create_user", "udf_name", "udf_type", "tree_id", "create_time", "update_time", "sys", "cluster_name", "is_expire", "is_shared"]
      |            }
      |        },
      |        {
      |            "name": "jdbc",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "test2",
      |                "url": "jdbc:mysql://127.0.0.1:3306/dip_linkis?characterEncoding=UTF-8",
      |                "driver": "com.mysql.jdbc.Driver",
      |                "user": "root",
      |                "password": "123456",
      |                "targetTable": "linkis_ps_udf_baseinfo2",
      |                "options": {
      |                }
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

  val groupConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "jdbc",
      |            "type": "",
      |            "config": {
      |                "resultTable": "test1",
      |                "url": "jdbc:mysql://127.0.0.1:3306/dip_linkis?characterEncoding=UTF-8",
      |                "driver": "com.mysql.jdbc.Driver",
      |                "user": "root",
      |                "password": "123456",
      |                "query": "select * from dip_linkis.linkis_ps_udf_baseinfo",
      |                "options": {
      |                }
      |            }
      |        },
      |        {
      |            "name": "file",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "test2",
      |                "path": "hdfs:///tmp/test_new_no_partition",
      |                "serializer": "csv",
      |                "columnNames": ["id", "create_user", "udf_name", "udf_type", "tree_id", "create_time", "update_time", "sys", "cluster_name", "is_expire", "is_shared"]
      |            }
      |        }
      |    ],
      |    "transformations": [
      |        {
      |            "name": "sql",
      |            "type": "transformation",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "sql": "select * from test1"
      |            }
      |        }
      |    ],
      |    "sinks": [
      |        {
      |            "name": "file",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "hdfs:///tmp/test_new",
      |                "partitionBy": ["create_user"],
      |                "saveMode": "overwrite",
      |                "serializer": "csv"
      |            }
      |        },
      |        {
      |            "name": "jdbc",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "test2",
      |                "url": "jdbc:mysql://127.0.0.1:3306/dip_linkis?characterEncoding=UTF-8",
      |                "driver": "com.mysql.jdbc.Driver",
      |                "user": "root",
      |                "password": "123456",
      |                "targetTable": "linkis_ps_udf_baseinfo2",
      |                "options": {
      |                }
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

}
