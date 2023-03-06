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

package org.apache.linkis.engineplugin.spark.datacalc.source

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcSource
import org.apache.linkis.engineplugin.spark.datacalc.exception.DataSourceNotConfigException
import org.apache.linkis.engineplugin.spark.datacalc.service.LinkisDataSourceService
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary

import org.apache.spark.sql.{Dataset, Row, SparkSession}

import java.text.MessageFormat

class ManagedJdbcSource extends DataCalcSource[ManagedJdbcSourceConfig] with Logging {

  override def getData(spark: SparkSession): Dataset[Row] = {
    val db = LinkisDataSourceService.getDatasource(config.getDatasource)
    if (db == null) {
      throw new DataSourceNotConfigException(
        SparkErrorCodeSummary.DATA_CALC_DATASOURCE_NOT_CONFIG.getErrorCode,
        MessageFormat.format(
          SparkErrorCodeSummary.DATA_CALC_DATASOURCE_NOT_CONFIG.getErrorDesc,
          config.getDatasource
        )
      )
    }

    val jdbcConfig = new JdbcSourceConfig()
    jdbcConfig.setUrl(db.getUrl)
    jdbcConfig.setDriver(db.getDriver)
    jdbcConfig.setUser(db.getUser)
    jdbcConfig.setPassword(db.getPassword)
    jdbcConfig.setQuery(config.getQuery)
    jdbcConfig.setPersist(config.getPersist)
    jdbcConfig.setOptions(config.getOptions)
    jdbcConfig.setResultTable(config.getResultTable)

    val sourcePlugin = new JdbcSource()
    sourcePlugin.setConfig(jdbcConfig)
    sourcePlugin.getData(spark)
  }

}
