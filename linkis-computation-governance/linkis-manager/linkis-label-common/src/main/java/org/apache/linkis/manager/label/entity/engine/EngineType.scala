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

package org.apache.linkis.manager.label.entity.engine

import org.apache.linkis.common.utils.Logging

import java.util

object EngineType extends Enumeration with Logging {

  type EngineType = Value

  val SPARK = Value("spark")

  val HIVE = Value("hive")

  val HBASE = Value("hbase")

  val PYTHON = Value("python")

  val REPL = Value("repl")

  val DORIS = Value("doris")

  val SHELL = Value("shell")

  val JDBC = Value("jdbc")

  val IO_ENGINE_FILE = Value("io_file")

  val IO_ENGINE_HDFS = Value("io_hdfs")

  val FPS = Value("fps")

  val PIPELINE = Value("pipeline")

  val PRESTO = Value("presto")

  val NEBULA = Value("nebula")

  val FLINK = Value("flink")

  val APPCONN = Value("appconn")

  val SQOOP = Value("sqoop")

  val DATAX = Value("datax")

  val OPENLOOKENG = Value("openlookeng")

  val TRINO = Value("trino")

  val ELASTICSEARCH = Value("elasticsearch")

  val SEATUNNEL = Value("seatunnel")

  val IMPALA = Value("impala")

  val JOBSERVER = Value("jobserver")

  def mapFsTypeToEngineType(fsType: String): String = {
    fsType match {
      case "file" =>
        EngineType.IO_ENGINE_FILE.toString
      case "hdfs" =>
        EngineType.IO_ENGINE_HDFS.toString
      case _ =>
        logger.error(s"In method mapFsTypeToEngineType(): Invalid fsType : ${fsType}, will not convert.")
        fsType
    }
  }

  /**
   * Please add it here when new engineType was added.
   */
  def mapStringToEngineType(str: String): EngineType = str match {
    case _ if null == str || "".equals(str) => null
    case _ if SPARK.toString.equalsIgnoreCase(str) => SPARK
    case _ if HIVE.toString.equalsIgnoreCase(str) => HIVE
    case _ if HBASE.toString.equalsIgnoreCase(str) => HBASE
    case _ if PYTHON.toString.equalsIgnoreCase(str) => PYTHON
    case _ if SHELL.toString.equalsIgnoreCase(str) => SHELL
    case _ if JDBC.toString.equalsIgnoreCase(str) => JDBC
    case _ if IO_ENGINE_FILE.toString.equalsIgnoreCase(str) => IO_ENGINE_FILE
    case _ if IO_ENGINE_HDFS.toString.equalsIgnoreCase(str) => IO_ENGINE_HDFS
    case _ if PIPELINE.toString.equalsIgnoreCase(str) => PIPELINE
    case _ if PRESTO.toString.equalsIgnoreCase(str) => PRESTO
    case _ if NEBULA.toString.equalsIgnoreCase(str) => NEBULA
    case _ if REPL.toString.equalsIgnoreCase(str) => REPL
    case _ if DORIS.toString.equalsIgnoreCase(str) => DORIS
    case _ if FLINK.toString.equalsIgnoreCase(str) => FLINK
    case _ if APPCONN.toString.equals(str) => APPCONN
    case _ if SQOOP.toString.equalsIgnoreCase(str) => SQOOP
    case _ if DATAX.toString.equalsIgnoreCase(str) => DATAX
    case _ if OPENLOOKENG.toString.equalsIgnoreCase(str) => OPENLOOKENG
    case _ if TRINO.toString.equalsIgnoreCase(str) => TRINO
    case _ if ELASTICSEARCH.toString.equalsIgnoreCase(str) => ELASTICSEARCH
    case _ if SEATUNNEL.toString.equalsIgnoreCase(str) => SEATUNNEL
    case _ => null

  }

  def getAllEngineTypes(): util.List[EngineType] = {
    val list = new util.ArrayList[EngineType]()
    EngineType.values.foreach(list.add)
    list
  }

}
