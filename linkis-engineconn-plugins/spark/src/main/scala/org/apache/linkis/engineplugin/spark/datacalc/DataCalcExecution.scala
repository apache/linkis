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

package org.apache.linkis.engineplugin.spark.datacalc

import org.apache.linkis.engineplugin.spark.datacalc.api.{
  DataCalcPlugin,
  DataCalcSink,
  DataCalcSource,
  DataCalcTransform
}
import org.apache.linkis.engineplugin.spark.datacalc.exception.ConfigRuntimeException
import org.apache.linkis.engineplugin.spark.datacalc.model._
import org.apache.linkis.engineplugin.spark.datacalc.util.PluginUtil
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.storage.StorageLevel

import javax.validation.{Validation, Validator}

import java.text.MessageFormat

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.slf4j.{Logger, LoggerFactory}

object DataCalcExecution {

  private val log: Logger = LoggerFactory.getLogger(DataCalcExecution.getClass)

  def getPlugins[SR <: SourceConfig, TR <: TransformConfig, SK <: SinkConfig](
      mapleData: DataCalcGroupData
  ): (Array[DataCalcSource[SR]], Array[DataCalcTransform[TR]], Array[DataCalcSink[SK]]) = {
    val sources = mapleData.getSources.map(source =>
      PluginUtil.createSource[SR](source.getName, source.getConfig)
    )
    val transformations = if (mapleData.getTransformations == null) {
      Array.empty[DataCalcTransform[TR]]
    } else {
      mapleData.getTransformations.map(sink =>
        PluginUtil.createTransform[TR](sink.getName, sink.getConfig)
      )
    }
    val sinks =
      mapleData.getSinks.map(sink => PluginUtil.createSink[SK](sink.getName, sink.getConfig))

    val checkResult = new CheckResult()
    sources.foreach(source => {
      source.getConfig.setVariables(mapleData.getVariables)
      checkResult.checkResultTable(source)
    })
    transformations.foreach(transformation => {
      transformation.getConfig.setVariables(mapleData.getVariables)
      checkResult.checkResultTable(transformation)
    })
    sinks.foreach(sink => {
      sink.getConfig.setVariables(mapleData.getVariables)
      checkResult.checkPluginConfig(sink)
    })
    checkResult.check()

    (sources, transformations, sinks)
  }

  def execute[SR <: SourceConfig, TR <: TransformConfig, SK <: SinkConfig](
      spark: SparkSession,
      sources: Array[DataCalcSource[SR]],
      transformations: Array[DataCalcTransform[TR]],
      sinks: Array[DataCalcSink[SK]]
  ): Unit = {
    if (sources != null && !sources.isEmpty) sources.foreach(source => sourceProcess(spark, source))
    if (transformations != null && !transformations.isEmpty) {
      transformations.foreach(transformation => transformProcess(spark, transformation))
    }
    if (sinks != null && !sinks.isEmpty) sinks.foreach(sink => sinkProcess(spark, sink))

    DataCalcTempData.clean(spark.sqlContext)
  }

  def getPlugins[SR <: SourceConfig, TR <: TransformConfig, SK <: SinkConfig, T <: Object](
      mapleData: DataCalcArrayData
  ): Array[Any] = {
    val checkResult = new CheckResult()
    val plugins = new Array[Any](mapleData.getPlugins.length)
    for (i <- mapleData.getPlugins.indices) {
      val config = mapleData.getPlugins()(i)
      config.getType match {
        case "source" =>
          val source = PluginUtil.createSource[SR](config.getName, config.getConfig)
          source.getConfig.setVariables(mapleData.getVariables)
          checkResult.checkResultTable(source)
          plugins(i) = source
        case "transformation" =>
          val transformation = PluginUtil.createTransform[TR](config.getName, config.getConfig)
          transformation.getConfig.setVariables(mapleData.getVariables)
          checkResult.checkResultTable(transformation)
          plugins(i) = transformation
        case "sink" =>
          val sink = PluginUtil.createSink[SK](config.getName, config.getConfig)
          sink.getConfig.setVariables(mapleData.getVariables)
          checkResult.checkPluginConfig(sink)
          plugins(i) = sink
        case t: String =>
          throw new ConfigRuntimeException(
            SparkErrorCodeSummary.DATA_CALC_CONFIG_TYPE_NOT_VALID.getErrorCode,
            MessageFormat.format(
              SparkErrorCodeSummary.DATA_CALC_CONFIG_TYPE_NOT_VALID.getErrorDesc,
              t
            )
          )
      }
    }
    checkResult.check()
    plugins
  }

  def execute[SR <: SourceConfig, TR <: TransformConfig, SK <: SinkConfig, T <: Object](
      spark: SparkSession,
      plugins: Array[Any]
  ): Unit = {
    if (plugins == null || plugins.isEmpty) return
    plugins.foreach {
      case source: DataCalcSource[SR] => sourceProcess(spark, source)
      case transform: DataCalcTransform[TR] => transformProcess(spark, transform)
      case sink: DataCalcSink[SK] => sinkProcess(spark, sink)
      case _ =>
    }

    DataCalcTempData.clean(spark.sqlContext)
  }

  private def sourceProcess[T <: SourceConfig](
      spark: SparkSession,
      source: DataCalcSource[T]
  ): Unit = {
    source.prepare(spark)
    val ds: Dataset[Row] = source.getData(spark)
    tempSaveResultTable(ds, source.getConfig)
  }

  private def transformProcess[T <: TransformConfig](
      spark: SparkSession,
      transform: DataCalcTransform[T]
  ): Unit = {
    transform.prepare(spark)
    val fromDs: Dataset[Row] = if (StringUtils.isNotBlank(transform.getConfig.getSourceTable)) {
      spark.read.table(transform.getConfig.getSourceTable)
    } else {
      null
    }
    val ds: Dataset[Row] = transform.process(spark, fromDs)
    tempSaveResultTable(ds, transform.getConfig)
  }

  private def sinkProcess[T <: SinkConfig](spark: SparkSession, sink: DataCalcSink[T]): Unit = {
    sink.prepare(spark)
    val fromDs: Dataset[Row] = if (StringUtils.isBlank(sink.getConfig.getSourceQuery)) {
      spark.read.table(sink.getConfig.getSourceTable)
    } else {
      spark.sql(sink.getConfig.getSourceQuery)
    }
    sink.output(spark, fromDs)
  }

  private def tempSaveResultTable(ds: Dataset[Row], resultTableConfig: ResultTableConfig): Unit = {
    if (ds != null) {
      ds.createOrReplaceTempView(resultTableConfig.getResultTable)
      DataCalcTempData.putResultTable(resultTableConfig.getResultTable)
      if (resultTableConfig.getPersist) {
        ds.persist(StorageLevel.fromString(resultTableConfig.getStorageLevel))
        DataCalcTempData.putPersistDataSet(ds)
      }
    }
  }

  private class CheckResult {

    private var success: Boolean = true
    private val set: mutable.Set[String] = mutable.Set()

    val validator: Validator = Validation.buildDefaultValidatorFactory().getValidator

    def checkResultTable[T <: ResultTableConfig](plugin: DataCalcPlugin[T]): Unit = {
      checkPluginConfig(plugin)
      if (set.contains(plugin.getConfig.getResultTable)) {
        log.error(s"Result table [${plugin.getConfig.getResultTable}] cannot be duplicate")
        success = false
      } else {
        set.add(plugin.getConfig.getResultTable)
      }
    }

    def checkPluginConfig[T](plugin: DataCalcPlugin[T]): Unit = {
      val violations = validator.validate(plugin.getConfig)
      if (!violations.isEmpty) {
        success = false
        log.error(
          s"Configuration check error, ${BDPJettyServerHelper.gson.toJson(plugin.getConfig)}"
        )
        for (violation <- violations.asScala) {
          if (
              violation.getMessageTemplate
                .startsWith("{") && violation.getMessageTemplate.endsWith("}")
          ) {
            log.error(s"[${violation.getPropertyPath}] ${violation.getMessage}")
          } else {
            log.error(violation.getMessage)
          }
        }
      }
    }

    def check(): Unit = {
      if (!success) {
        throw new ConfigRuntimeException(
          SparkErrorCodeSummary.DATA_CALC_CONFIG_VALID_FAILED.getErrorCode,
          SparkErrorCodeSummary.DATA_CALC_CONFIG_VALID_FAILED.getErrorDesc
        )
      }
    }

  }

}
