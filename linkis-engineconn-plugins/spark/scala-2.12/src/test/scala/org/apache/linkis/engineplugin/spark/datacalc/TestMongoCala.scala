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

class TestMongoCala {

  val filePath = this.getClass.getResource("/").getFile

  @Test
  def testMongoWrite: Unit = {
    // skip os: windows
    if (!FsPath.WINDOWS) {
      val data = DataCalcGroupData.getData(mongoWriteConfigJson.replace("{filePath}", filePath))
      Assertions.assertTrue(data != null)

      val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)
      Assertions.assertTrue(sources != null)
      Assertions.assertTrue(transforms != null)
      Assertions.assertTrue(sinks != null)
    }
  }

  @Test
  def testMongoReader: Unit = {
    // skip os: windows
    if (!FsPath.WINDOWS) {
      val data = DataCalcGroupData.getData(mongoReaderConfigJson.replace("{filePath}", filePath))
      Assertions.assertTrue(data != null)

      val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)
      Assertions.assertTrue(sources != null)
      Assertions.assertTrue(transforms != null)
      Assertions.assertTrue(sinks != null)
    }
  }

  val mongoWriteConfigJson =
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
      |            "name": "mongo",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "uri": "mongodb://localhost:27017/test",
      |                "database": "test",
      |                "collection": "test",
      |                "saveMode": "overwrite"
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

  val mongoReaderConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "mongo",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "uri": "mongodb://localhost:27017/test",
      |                "database": "test",
      |                "collection": "test"
      |            }
      |        }
      |    ],
      |    "sinks": [
      |        {
      |            "name": "file",
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
