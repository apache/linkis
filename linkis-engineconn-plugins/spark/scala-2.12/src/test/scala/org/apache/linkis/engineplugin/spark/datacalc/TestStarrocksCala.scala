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

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.engineplugin.spark.datacalc.model.DataCalcGroupData

import org.junit.jupiter.api.{Assertions, Test};

class TestStarrocksCala {

  val filePath = this.getClass.getResource("/").getFile

  @Test
  def testStarrocksWrite: Unit = {
    // skip os: windows
    if (!FsPath.WINDOWS) {
      // todo The starrocks connector currently only supports the 'append' mode, using the starrocks 'Primary Key table' to do 'upsert'
      val data = DataCalcGroupData.getData(starrocksWriteConfigJson.replace("{filePath}", filePath))
      Assertions.assertTrue(data != null)

      val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)
      Assertions.assertTrue(sources != null)
      Assertions.assertTrue(transforms != null)
      Assertions.assertTrue(sinks != null)
    }
  }

  @Test
  def testStarrocksReader: Unit = {
    // skip os: windows
    if (!FsPath.WINDOWS) {
      val data =
        DataCalcGroupData.getData(starrocksReaderConfigJson.replace("{filePath}", filePath))
      Assertions.assertTrue(data != null)

      val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)
      Assertions.assertTrue(sources != null)
      Assertions.assertTrue(transforms != null)
      Assertions.assertTrue(sinks != null)
    }
  }

  val starrocksWriteConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "file",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "path": "file://{filePath}/etltest.dolphin",
      |                "serializer": "csv",
      |                "options": {
      |                "header":"true",
      |                "delimiter":";"
      |                },
      |                "columnNames": ["name", "age"]
      |            }
      |        }
      |    ],
      |    "sinks": [
      |        {
      |            "name": "starrocks",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "url": "localhost:8030",
      |                "jdbcUrl": "jdbc:mysql://localhost:9030",
      |                "user": "root",
      |                "password": "",
      |                "targetDatabase": "test",
      |                "targetTable": "test"
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

  val starrocksReaderConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "starrocks",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "url": "localhost:8030",
      |                "user": "root",
      |                "password": "",
      |                "sourceDatabase": "test",
      |                "sourceTable": "test"
      |            }
      |        }
      |    ],
      |    "sinks": [
      |        {
      |            "name": "file",
      |            "type": "sink",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "file://{filePath}/json",
      |                "saveMode": "overwrite",
      |                "serializer": "json"
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

}
