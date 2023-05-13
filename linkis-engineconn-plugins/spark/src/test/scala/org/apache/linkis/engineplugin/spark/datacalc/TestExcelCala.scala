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

import org.apache.linkis.engineplugin.spark.datacalc.model.DataCalcGroupData

import org.apache.spark.sql.SparkSession

import org.junit.jupiter.api.Test;

class TestExcelCala {

  @Test
  def testExcelWrite: Unit = {
    val data = DataCalcGroupData.getData(excelWriteConfigJson)

    val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)

    val spark = SparkSession
      .builder()
      .master("local")
      .getOrCreate()

    DataCalcExecution.execute(spark, sources, transforms, sinks)

  }

  @Test
  def testExcelReader: Unit = {
    val data = DataCalcGroupData.getData(excelReaderConfigJson)

    val (sources, transforms, sinks) = DataCalcExecution.getPlugins(data)

    val spark = SparkSession
      .builder()
      .master("local")
      .getOrCreate()

    DataCalcExecution.execute(spark, sources, transforms, sinks)

  }

  val excelWriteConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "file",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "path": "file:///Users/chengjie/cjtest/newfile.csv",
      |                "serializer": "csv",
      |                "options": {
      |                "header":"true",
      |                "delimiter":";"
      |                },
      |                "columnNames": ["name", "age"]
      |            }
      |        }
      |    ],
      |    "transformations": [
      |    ],
      |    "sinks": [
      |        {
      |            "name": "file",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "file:///Users/chengjie/cjtest/test.xlsx",
      |                "saveMode": "overwrite",
      |                "serializer": "excel"
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

  val excelReaderConfigJson =
    """
      |{
      |    "sources": [
      |        {
      |            "name": "file",
      |            "type": "source",
      |            "config": {
      |                "resultTable": "T1654611700631",
      |                "path": "file:///Users/chengjie/cjtest/test.xlsx",
      |                "serializer": "excel",
      |                "options": {
      |                "header":"true"
      |                },
      |                "columnNames": ["name", "age"]
      |            }
      |        }
      |    ],
      |    "transformations": [
      |    ],
      |    "sinks": [
      |        {
      |            "name": "file",
      |            "config": {
      |                "sourceTable": "T1654611700631",
      |                "path": "file:///Users/chengjie/cjtest/csv",
      |                "saveMode": "overwrite",
      |                "options": {
      |                "header":"true"
      |                },
      |                "serializer": "csv"
      |            }
      |        }
      |    ]
      |}
      |""".stripMargin

}
