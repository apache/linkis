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

package org.apache.linkis.governance.common.paser

import org.junit.jupiter.api.{DisplayName, Test}
import org.junit.jupiter.api.Assertions.assertTrue

class PythonCodeParseTest {

  @Test
  @DisplayName("testParsePythonWithSemicolon")
  def testParseSqlWithSemicolon(): Unit = {
    val parser = new PythonCodeParser
    val pythonCode: String =
      """
        |from cgitb import enable
        |import sys, datetime
        |from dateutil.relativedelta import relativedelta
        |from pyspark.sql import SparkSession
        |import requests
        |import json
        |import MySQLdb
        |def getSpark():
        |    spark = SparkSession \
        |        .builder \
        |        .appName("PySpark_Platform_DataLake11") \
        |        .config("spark.jars.packages","org.apache.iceberg:iceberg-spark3:0.13.2") \
        |        .config("spark.sql.extensions","org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions") \
        |        .config("spark.sql.catalog.spark_catalog","org.apache.iceberg.spark.SparkSessionCatalog") \
        |        .config("spark.sql.catalog.spark_catalog.type","hive") \
        |        .config("spark.sql.catalog.iceberg","org.apache.iceberg.spark.SparkCatalog") \
        |        .config("spark.sql.catalog.iceberg.type","hadoop") \
        |        .config("spark.sql.catalog.iceberg.warehouse","hdfs:///deecluster/datalake") \
        |        .enableHiveSupport() \
        |        .getOrCreate()
        |    return spark
        |""".stripMargin
    val strings = parser.parsePythonCode(pythonCode)
    assertTrue(strings.length == 8)
  }

  @Test
  @DisplayName("testParsePythonWithDecorator")
  def testParsePythonWithDecorator(): Unit = {
    val parser = new PythonCodeParser
    val pythonCode: String =
      """
        |import time
        |def timmer(func):
        |    def wrapper(*args, **kwargs):
        |        start = time.time()
        |        func(*args, **kwargs)
        |        stop = time.time()
        |        print('foo运行时长:', stop - start)
        |    return wrapper
        |@timmer
        |def foo(x, y, z):
        |    time.sleep(3)
        |    print(f'x={x}, y={y}, z={z}')
        |foo(1,2,3)
        """.stripMargin
    val strings = parser.parsePythonCode(pythonCode)
    assertTrue(strings.length == 4)
  }

}
