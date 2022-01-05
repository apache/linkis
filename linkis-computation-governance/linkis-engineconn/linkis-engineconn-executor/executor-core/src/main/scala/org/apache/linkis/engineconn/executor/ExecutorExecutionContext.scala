/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.executor

import java.util.concurrent.atomic.AtomicInteger

import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.label.entity.Label
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateFormatUtils

import scala.collection.mutable.ArrayBuffer


trait ExecutorExecutionContext {

  protected val resultSetWriters: ArrayBuffer[ResultSetWriter[_ <: MetaData, _ <: Record]] = ArrayBuffer[ResultSetWriter[_ <: MetaData, _ <: Record]]()

  private var interrupted = false

  private var jobId: Option[String] = None
  protected val aliasNum = new AtomicInteger(0)
  private var storePath: Option[String] = None

  private var labels: Array[Label[_]] = new Array[Label[_]](0)

  def kill(): Unit = interrupted = true

  def isKilled: Boolean = interrupted

  def setJobId(jobId: String): Unit = this.jobId = Option(jobId)

  def getJobId: Option[String] = jobId

  def setStorePath(storePath: String): Unit = this.storePath = Option(storePath)

  def getStorePath: Option[String] = this.storePath

  def getLabels: Array[Label[_]] = labels

  def setLabels(labels: Array[Label[_]]): Unit = this.labels = labels

  protected def getDefaultStorePath: String = {
    val path = GovernanceCommonConf.RESULT_SET_STORE_PATH.getValue
    val pathPrefix = (if (path.endsWith("/")) path else path + "/") + Utils.getJvmUser + "/" +
      DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd") + "/"
    getJobId.map(pathPrefix + _ + "/" + System.nanoTime).getOrElse(pathPrefix  + System.nanoTime)
  }

  protected def getResultSetPath(resultSet: ResultSet[_ <: MetaData, _ <: Record], alias: String): FsPath = {
    val filePath = storePath.getOrElse(getDefaultStorePath)
    val fileName = if (StringUtils.isEmpty(alias)) "_" + aliasNum.getAndIncrement() else alias + "_" + aliasNum.getAndIncrement()
    resultSet.getResultSetPath(new FsPath(filePath), fileName)
  }

  protected def getResultSetByType(resultSetType: String): ResultSet[_ <: MetaData, _ <: Record]
  protected def getDefaultResultSetByType: String

  def createDefaultResultSetWriter(): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    createResultSetWriter(getResultSetByType(getDefaultResultSetByType)) // todo check
  }

  def createDefaultResultSetWriter(alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(getResultSetByType(getDefaultResultSetByType), alias) // todo check

  def createResultSetWriter(resultSetType: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(getResultSetByType(resultSetType), null)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record]): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSet, null)

  def createResultSetWriter(resultSetType: String, alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(getResultSetByType(resultSetType), alias)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record], alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    val resultSetPath = getResultSetPath(resultSet, alias)
    val resultSetWriter = newResultSetWriter(resultSet, resultSetPath, alias)
    resultSetWriters synchronized resultSetWriters += resultSetWriter
    resultSetWriter
  }

  protected def newResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record],
                                   resultSetPath: FsPath,
                                   alias: String): ResultSetWriter[_ <: MetaData, _ <: Record]

  def close(): Unit = {
    resultSetWriters.toArray.foreach(_.close())
    resultSetWriters.clear()
  }

  def setResultSetNum(num: Int): Unit = aliasNum.set(num)

}
