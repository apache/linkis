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

object RunType extends Enumeration {

  type RunType = Value
  val SQL = Value("sql")
  val HIVE = Value("hql")
  val SCALA = Value("scala")
  val PYTHON = Value("python")
  val REPL = Value("repl")
  val DORIS = Value("doris")
  val JAVA = Value("java")
  val PYSPARK = Value("py")
  val R = Value("r")
  val STORAGE = Value("out")
  val SHELL = Value("shell")
  val IO_FILE = Value("io_file")
  val IO_HDFS = Value("io_hdfs")
  val FPS = Value("fps")
  val PIPELINE = Value("pipeline")
  val JDBC = Value("jdbc")
  val PRESTO_SQL = Value("psql")
  val NEBULA_SQL = Value("ngql")
  val JAR = Value("jar")
  val APPCONN = Value("appconn")
  val FUNCTION_MDQ_TYPE = Value("function.mdq")
  val ES_SQL = Value("essql")
  val ES_JSON = Value("esjson")

  val TRINO_SQL = Value("tsql")
  val JSON = Value("json")


  val SEATUNNEL_ZETA = Value("szeta")
  val SEATUNNEL_FLINK = Value("sflink")
  val SEATUNNEL_SPARK = Value("sspark")

  val DATA_CALC = Value("data_calc") // spark datacalc (ETL)

  val IMPALA_SQL = Value("isql")
}
