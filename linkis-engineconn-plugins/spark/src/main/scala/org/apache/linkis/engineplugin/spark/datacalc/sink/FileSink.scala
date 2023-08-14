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

package org.apache.linkis.engineplugin.spark.datacalc.sink

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcSink

import org.apache.commons.text.StringSubstitutor
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.JavaConverters._

class FileSink extends DataCalcSink[FileSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    val writer = ds.write.mode(config.getSaveMode)

    if (config.getPartitionBy != null && !config.getPartitionBy.isEmpty) {
      val partitionKeys = config.getPartitionBy.asScala
      writer.partitionBy(partitionKeys: _*)
    }

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      writer.options(config.getOptions)
    }
    val substitutor = new StringSubstitutor(config.getVariables)
    val path = substitutor.replace(config.getPath)
    logger.info(s"Save data to file, path: $path")

    config.getSerializer match {
      case "csv" => writer.csv(path)
      case "json" => writer.json(path)
      case "parquet" => writer.parquet(path)
      case "text" => writer.text(path)
      case "orc" => writer.orc(path)
      case "excel" => writer.format("excel").save(path)
      case _ => writer.format(config.getSerializer).save(path)
    }
  }

}
