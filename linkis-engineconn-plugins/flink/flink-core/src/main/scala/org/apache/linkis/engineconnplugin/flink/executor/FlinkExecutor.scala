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

package org.apache.linkis.engineconnplugin.flink.executor

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSetWriter
import org.apache.linkis.common.utils.OverloadUtils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.executor.entity.{
  KubernetesExecutor,
  LabelExecutor,
  ResourceExecutor,
  YarnExecutor
}
import org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet
import org.apache.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration
import org.apache.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration.LINKIS_FLINK_CLIENT_CORES
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.engineconnplugin.flink.util.FlinkValueFormatUtil
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.flink.configuration.{CoreOptions, JobManagerOptions, TaskManagerOptions}
import org.apache.flink.kubernetes.configuration.KubernetesConfigOptions
import org.apache.flink.types.Row
import org.apache.flink.yarn.configuration.YarnConfigOptions

import java.util

trait FlinkExecutor
    extends YarnExecutor
    with KubernetesExecutor
    with LabelExecutor
    with ResourceExecutor {

  private var jobID: String = _
  private var applicationId: String = _
  private var kubernetesClusterID: String = _
  private var applicationURL: String = _
  private var yarnMode: String = "Client"
  private var queue: String = _
  private var namespace: String = _

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]

  def getJobID: String = jobID

  protected def setJobID(jobID: String): Unit = {
    this.jobID = jobID
  }

  override def getApplicationId: String = applicationId
  def setApplicationId(applicationId: String): Unit = this.applicationId = applicationId

  override def getKubernetesClusterID: String = kubernetesClusterID

  def setKubernetesClusterID(kubernetesClusterID: String): Unit = this.kubernetesClusterID =
    kubernetesClusterID

  override def getApplicationURL: String = applicationURL
  def setApplicationURL(applicationURL: String): Unit = this.applicationURL = applicationURL

  override def getYarnMode: String = yarnMode
  def setYarnMode(yarnMode: String): Unit = this.yarnMode = yarnMode

  override def getQueue: String = queue
  def setQueue(queue: String): Unit = this.queue = queue

  override def getNamespace: String = namespace

  def setNamespace(namespace: String): Unit = this.namespace = namespace

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource =
    throw new JobExecutionException(NOT_SUPPORT_METHOD.getErrorDesc)

  protected val flinkEngineConnContext: FlinkEngineConnContext

  queue = flinkEngineConnContext.getEnvironmentContext.getFlinkConfig.get(
    YarnConfigOptions.APPLICATION_QUEUE
  )

  namespace = flinkEngineConnContext.getEnvironmentContext.getFlinkConfig.get(
    KubernetesConfigOptions.NAMESPACE
  )

  override def getCurrentNodeResource(): NodeResource = {
    val flinkConfig = flinkEngineConnContext.getEnvironmentContext.getFlinkConfig
    val jobManagerMemory = flinkConfig.get(JobManagerOptions.TOTAL_PROCESS_MEMORY).getBytes
    val taskManagerMemory = flinkConfig.get(TaskManagerOptions.TOTAL_PROCESS_MEMORY).getBytes
    val parallelism = flinkConfig.get(CoreOptions.DEFAULT_PARALLELISM)
    val numOfTaskSlots = flinkConfig.get(TaskManagerOptions.NUM_TASK_SLOTS)
    val containers = Math.round(parallelism * 1.0f / numOfTaskSlots)
    val yarnMemory = taskManagerMemory * containers + jobManagerMemory
    val yarnCores =
      FlinkResourceConfiguration.LINKIS_FLINK_TASK_MANAGER_CPU_CORES.getValue * containers + 1
    val resource = new DriverAndYarnResource(
      new LoadInstanceResource(OverloadUtils.getProcessMaxMemory, LINKIS_FLINK_CLIENT_CORES, 1),
      new YarnResource(yarnMemory, yarnCores, 0, queue)
    )
    val engineResource = new CommonNodeResource
    engineResource.setUsedResource(resource)
    engineResource.setResourceType(ResourceUtils.getResourceTypeByResource(resource))
    engineResource
  }

  def supportCallBackLogs(): Boolean = true
}

object FlinkExecutor {

  import scala.collection.JavaConverters._

  def writeResultSet(
      resultSet: ResultSet,
      resultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record]
  ): Unit = {
    val columns = resultSet.getColumns.asScala
      .map(columnInfo =>
        new Column(columnInfo.getName, DataType.toDataType(columnInfo.getType), null)
      )
      .toArray
    resultSetWriter.addMetaData(new TableMetaData(columns))
    resultSet.getData match {
      case data: util.List[Row] =>
        data.asScala.foreach { row =>
          val record =
            (0 until row.getArity).map(row.getField).map(FlinkValueFormatUtil.formatValue).toArray
          resultSetWriter.addRecord(new TableRecord(record.asInstanceOf[Array[AnyRef]]))
        }
      case _ =>
    }
  }

  def writeAndSendResultSet(
      resultSet: ResultSet,
      engineExecutionContext: EngineExecutionContext
  ): Unit = {
    val resultSetWriter =
      engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    writeResultSet(resultSet, resultSetWriter)
    engineExecutionContext.sendResultSet(resultSetWriter)
  }

}
