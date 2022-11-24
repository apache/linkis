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

package org.apache.linkis.engineplugin.spark.datacalc.transform

import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcTransform

import org.apache.spark.sql.{Dataset, Row, SparkSession}

import org.slf4j.{Logger, LoggerFactory}

class SqlTransform extends DataCalcTransform[SqlTransformConfig] {

  private val log: Logger = LoggerFactory.getLogger(classOf[SqlTransform])

  override def process(spark: SparkSession, ds: Dataset[Row]): Dataset[Row] = {
    log.info(s"Load data from query: ${config.getSql}")
    spark.sql(config.getSql)
  }

}
