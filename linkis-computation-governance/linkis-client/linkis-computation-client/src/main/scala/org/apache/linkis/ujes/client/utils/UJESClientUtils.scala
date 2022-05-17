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
 
package org.apache.linkis.ujes.client.utils

import org.apache.linkis.ujes.client.request.JobExecuteAction.{EngineType, RunType}

object UJESClientUtils {
  def toEngineType(engineType: String): EngineType = engineType match {
    case "spark" => EngineType.SPARK
    case "hive" => EngineType.HIVE
    case "shell" => EngineType.SHELL
    case "python" => EngineType.PYTHON
    case _ => EngineType.SPARK
  }

  /**
    * TODO At first Judge engine type
    * @param runType
    * @param engineType
    * @return
    */
  def toRunType(runType:String, engineType: EngineType) : RunType = runType match {
    case "sql" => EngineType.SPARK.SQL
    case "pyspark" => EngineType.SPARK.PYSPARK
    case "scala" => EngineType.SPARK.SCALA
    case "r" => EngineType.SPARK.R
    case "hql" => EngineType.HIVE.HQL
    case "shell" => EngineType.SHELL.SH
    case "python" => EngineType.PYTHON.PY
    case _ => EngineType.SPARK.SQL
  }

}
