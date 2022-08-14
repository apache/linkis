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

package org.apache.linkis.engineplugin.spark.lineage

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineplugin.spark.extension.SparkSqlExtension

import org.apache.spark.sql.{DataFrame, SQLContext}

class LineageSparkSqlExtension extends SparkSqlExtension with Logging {

  override protected def extensionRule(
      sqlContext: SQLContext,
      command: String,
      dataFrame: DataFrame,
      sqlStartTime: Long
  ): Unit = {
    Utils.tryCatch {
      val conf = sqlContext.sparkContext.getConf
      val lineageEnabled = conf.getBoolean("spark.bdp.lineage.enabled", true)
      val startTime = System.currentTimeMillis
      logger.info("LineageExtractor extract start")
      if (lineageEnabled) SparkLineageUtils.extract(sqlContext, command, sqlStartTime, dataFrame)
      logger.info("LineageExtractor extract cost: " + (System.currentTimeMillis - startTime))
    } { cause: Throwable =>
      logger.info("Failed to collect lineage info:", cause)
    }

  }

}
