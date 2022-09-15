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

package org.apache.linkis.engineplugin.spark.metadata

import org.apache.linkis.common.utils.Logging

import org.apache.spark.sql.{DataFrame, Dataset, SparkLogicalPlanHelper, SQLContext}

/**
 * Description:
 */
class MetaDataInfoTool extends Logging {

  def getMetaDataInfo(sqlContext: SQLContext, sql: String, dataFrame: DataFrame): String = {
    logger.info(s"begin to get sql metadata info: ${cutSql(sql)}")
    val startTime = System.currentTimeMillis
    val inputTables =
      SparkLogicalPlanHelper.extract(sqlContext, sql, dataFrame.queryExecution, startTime)
    logger.info(s"end to get sql metadata info: ${cutSql(sql)}, metadata is ${inputTables}")
    if (inputTables != null) inputTables.toString else ""
  }

  private def cutSql(sql: String): String = {
    if (sql.length >= 1024) sql.substring(0, 1024) else sql
  }

}

object MetaDataInfoTool {

  def getMetaDataInfo(sqlContext: SQLContext, sql: String, dataFrame: DataFrame): String = {
    new MetaDataInfoTool().getMetaDataInfo(sqlContext, sql, dataFrame)
  }

}
