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
 
package org.apache.linkis.engineplugin.spark.executor

import java.text.NumberFormat

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.exception.SparkEngineException
import org.apache.linkis.engineplugin.spark.utils.EngineUtils
import org.apache.linkis.governance.common.exception.LinkisJobRetryException
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.lang.StringUtils
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{StructField, StructType}

import scala.collection.mutable.ArrayBuffer


object SQLSession extends Logging {
  val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(SparkConfiguration.SPARK_NF_FRACTION_LENGTH.getValue)

  def showDF(sc: SparkContext, jobGroup: String, dataFrame: DataFrame, alias: String, maxResult: Int, engineExecutionContext: EngineExecutionContext): Unit = {
    //
    if (sc.isStopped) {
      logger.error("Spark application has already stopped in showDF, please restart it.")
      throw new LinkisJobRetryException("Spark application sc has already stopped, please restart it.")
    }
    val startTime = System.currentTimeMillis()
    //    sc.setJobGroup(jobGroup, "Get IDE-SQL Results.", false)

    val iterator = Utils.tryThrow(dataFrame.toLocalIterator) { t =>
      throw new SparkEngineException(40002, s"dataFrame to local exception", t)
    }
    //var columns: List[Attribute] = null
    // get field names
    //logger.info("SCHEMA BEGIN")
    import java.util
    val colSet = new util.HashSet[String]()
    val schema = dataFrame.schema
    var columnsSet:StructType = null
    schema foreach (s => colSet.add(s.name))
    if (colSet.size() < schema.size){
      val arr:ArrayBuffer[StructField] = new ArrayBuffer[StructField]()
      val tmpSet = new util.HashSet[StructField]()
      val tmpArr = new ArrayBuffer[StructField]()
      dataFrame.queryExecution.analyzed.output foreach {
        attri => val tempAttri = StructField(attri.qualifiedName, attri.dataType, attri.nullable, attri.metadata)
          tmpSet.add(tempAttri)
          tmpArr += tempAttri
      }
      if (tmpSet.size() < schema.size){
        dataFrame.queryExecution.analyzed.output foreach {
          attri => val tempAttri = StructField(attri.toString(), attri.dataType, attri.nullable, attri.metadata)
            arr += tempAttri
        }
      }else{
        tmpArr.foreach(arr += _)
      }
      columnsSet = StructType(arr.toArray)
    }else{
      columnsSet = schema
    }
    //val columnsSet = dataFrame.schema
    val columns = columnsSet.map(c =>
      Column(c.name, DataType.toDataType(c.dataType.typeName.toLowerCase), c.getComment().orNull)).toArray[Column]
    columns.foreach(c => logger.info(s"c is ${c.columnName}, comment is ${c.comment}"))
    if (columns == null || columns.isEmpty) return
    val metaData = new TableMetaData(columns)
    val writer = if (StringUtils.isNotBlank(alias))
      engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE, alias)
    else engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    writer.addMetaData(metaData)
    var index = 0
    Utils.tryThrow({
      while (index < maxResult && iterator.hasNext) {
        val row = iterator.next()
        val r: Array[Any] = columns.indices.map { i =>
          val data = row(i) match {
            case value: String => value.replaceAll("\n|\t", " ")
            case value: Double => nf.format(value)
            case value: Any => value.toString
            case _ => null
          }
          data
        }.toArray
        writer.addRecord(new TableRecord(r))
        index += 1
      }
    }) { t =>
      throw new SparkEngineException(40001, s"read record  exception", t)
    }
    logger.warn(s"Time taken: ${System.currentTimeMillis() - startTime}, Fetched $index row(s).")
    //to register TempTable
    //Utils.tryAndErrorMsg(CSTableRegister.registerTempTable(engineExecutorContext, writer, alias, columns))("Failed to register tmp table:")
    engineExecutionContext.appendStdout(s"${EngineUtils.getName} >> Time taken: ${System.currentTimeMillis() - startTime}, Fetched $index row(s).")
    engineExecutionContext.sendResultSet(writer)
  }

  def showHTML(sc: SparkContext, jobGroup: String, htmlContent: Any, engineExecutionContext: EngineExecutionContext): Unit = {
    val startTime = System.currentTimeMillis()
    val writer = engineExecutionContext.createResultSetWriter(ResultSetFactory.HTML_TYPE)
    val metaData = new LineMetaData(null)
    writer.addMetaData(metaData)
    writer.addRecord(new LineRecord(htmlContent.toString))
    logger.warn(s"Time taken: ${System.currentTimeMillis() - startTime}")

    engineExecutionContext.sendResultSet(writer)
  }
}

