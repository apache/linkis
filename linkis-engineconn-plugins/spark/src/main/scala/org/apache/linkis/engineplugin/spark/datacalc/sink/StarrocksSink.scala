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

import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.JavaConverters._

class StarrocksSink extends DataCalcSink[StarrocksSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    var options = Map(
      "spark.starrocks.conf" -> "write",
      "spark.starrocks.write.fe.urls.http" -> config.getUrl,
      "spark.starrocks.write.fe.urls.jdbc" -> config.getJdbcUrl,
      "spark.starrocks.write.username" -> config.getUser,
      "spark.starrocks.write.password" -> config.getPassword,
      "spark.starrocks.write.properties.ignore_json_size" -> "true",
      "spark.starrocks.write.database" -> config.getTargetDatabase,
      "spark.starrocks.write.table" -> config.getTargetTable
    )

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      options = config.getOptions.asScala.toMap ++ options
    }

    // todo The starrocks connector currently only supports the 'append' mode, using the starrocks 'Primary Key table' to do 'upsert'
    val writer = ds.write.format("starrocks_writer").mode("append")

    logger.info(
      s"Save data from starrocks url: ${config.getUrl}, targetDatabase: ${config.getTargetDatabase}, targetTable: ${config.getTargetTable}"
    )
    writer.options(options).save()
  }

}
