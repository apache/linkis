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
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}

import java.sql.Connection

import scala.collection.JavaConverters._

class JdbcSink extends DataCalcSink[JdbcSinkConfig] with Logging {

  def output(spark: SparkSession, ds: Dataset[Row]): Unit = {
    val targetTable =
      if (StringUtils.isBlank(config.getTargetDatabase)) config.getTargetTable
      else config.getTargetDatabase + "." + config.getTargetTable
    var options = Map(
      "url" -> config.getUrl,
      "driver" -> config.getDriver,
      "user" -> config.getUser,
      "password" -> config.getPassword,
      "dbtable" -> targetTable,
      "connectionCollation" -> "utf8mb4_unicode_ci"
    )

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      options = config.getOptions.asScala.toMap ++ options
    }

    options = options ++ Map(
      "isolationLevel" -> options.getOrElse("isolationLevel", "NONE"),
      "batchsize" -> options.getOrElse("batchsize", "5000")
    )

    if (config.getPreQueries != null && !config.getPreQueries.isEmpty) {
      spark
        .sql("select 1")
        .repartition(1)
        .foreachPartition((_: Iterator[Row]) => {
          val jdbcOptions = new JDBCOptions(options)
          val conn: Connection = JdbcUtils.createConnectionFactory(jdbcOptions)()
          try {
            config.getPreQueries.asScala.foreach(query => {
              logger.info(s"Execute pre query: $query")
              execute(conn, jdbcOptions, query)
            })
          } catch {
            case e: Exception => logger.error("Execute preQueries failed. ", e)
          } finally {
            conn.close()
          }
        })
    }

    val writer = ds.repartition(config.getNumPartitions).write.format("jdbc")
    if (StringUtils.isNotBlank(config.getSaveMode)) {
      writer.mode(config.getSaveMode)
    }
    logger.info(
      s"Save data to jdbc url: ${config.getUrl}, driver: ${config.getDriver}, username: ${config.getUser}, table: $targetTable"
    )
    writer.options(options).save()
  }

  private def execute(conn: Connection, jdbcOptions: JDBCOptions, query: String): Unit = {
    logger.info("Execute query: {}", query)
    val statement = conn.prepareStatement(query)
    try {
      statement.setQueryTimeout(jdbcOptions.queryTimeout)
      val rows = statement.executeUpdate()
      logger.info("{} rows affected", rows)
    } catch {
      case e: Exception => logger.error("Execute query failed. ", e)
    } finally {
      statement.close()
    }
  }

}
