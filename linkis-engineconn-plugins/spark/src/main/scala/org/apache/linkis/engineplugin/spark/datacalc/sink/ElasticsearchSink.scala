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
import org.apache.linkis.engineplugin.spark.datacalc.exception.ElasticsearchSinkException
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.JavaConverters._

class ElasticsearchSink extends DataCalcSink[ElasticsearchSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    var options = Map(
      "es.index.auto.create" -> "true",
      "es.nodes.wan.only" -> "true",
      "es.nodes" -> config.getNode,
      "es.port" -> config.getPort,
      "es.net.http.auth.user" -> config.getUser,
      "es.net.http.auth.pass" -> config.getPassword
    )

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      options = config.getOptions.asScala.toMap ++ options
    }

    if (config.getSaveMode.equalsIgnoreCase("upsert")) {
      if (StringUtils.isBlank(config.getPrimaryKey)) {
        throw new ElasticsearchSinkException(
          SparkErrorCodeSummary.DATA_CALC_VARIABLE_NOT_EXIST.getErrorCode,
          "saveMode is upsert, please set elasticsearch mapping [primaryKey] in variables"
        )
      }
      options =
        options ++ Map("es.write.operation" -> "upsert", "es.mapping.id" -> config.getPrimaryKey)
      config.setSaveMode("append")
    }

    val writer = ds.write.format("org.elasticsearch.spark.sql")
    if (StringUtils.isNotBlank(config.getSaveMode)) {
      writer.mode(config.getSaveMode)
    }

    logger.info(
      s"Load data to elasticsearch nodes: ${config.getNode}, port: ${config.getPort}, index: ${config.getIndex}"
    )
    writer.options(options).save(s"${config.getIndex}/${config.getType}")
  }

}
