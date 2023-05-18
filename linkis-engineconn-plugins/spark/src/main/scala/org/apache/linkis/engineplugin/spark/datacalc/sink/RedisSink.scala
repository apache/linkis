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

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.JavaConverters._

class RedisSink extends DataCalcSink[RedisSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    var options = Map(
      "host" -> config.getHost,
      "port" -> config.getPort,
      "dbNum" -> config.getDbNum,
      "auth" -> config.getAuth,
      "table" -> config.getTargetTable
    )

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      options = config.getOptions.asScala.toMap ++ options
    }

    val writer = ds.write.format("org.apache.spark.sql.redis")
    if (StringUtils.isNotBlank(config.getSaveMode)) {
      writer.mode(config.getSaveMode)
    }
    logger.info(
      s"Save data to reids host: ${config.getHost}, port: ${config.getPort}, dbNum: ${config.getDbNum}, table: ${config.getTargetTable}"
    )
    writer.options(options).save()
  }

}
