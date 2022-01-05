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
 
package org.apache.linkis.storage.source

import java.io.Closeable
import java.util

import org.apache.linkis.common.io.{FsReader, FsWriter, MetaData, Record}
import org.apache.linkis.storage.domain.Column
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.script.{ScriptMetaData, VariableParser}
import org.apache.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.math3.util.Pair

import scala.collection.JavaConversions._


class FileSplit(var fsReader: FsReader[_ <: MetaData, _ <: Record], var `type`: String = "script/text") extends Closeable {

  var start: Int = 0

  var end: Int = -1

  var count: Int = 0

  var totalLine = 0

  var shuffler: Record => Record = r => r

  var pageTrigger: Boolean = false

  var params: util.Map[String, String] = new util.HashMap[String, String]

  def page(page: Int, pageSize: Int): Unit = {
    if (!pageTrigger) {
      start = (page - 1) * pageSize
      end = pageSize * page - 1
      pageTrigger = true
    }
  }

  def addParams(params: util.Map[String, String]): Unit = {
    this.params.putAll(params)
  }

  def addParams(key: String, value: String): Unit = {
    this.params += key -> value
  }

  def `while`[M](m: MetaData => M, r: Record => Unit): M = {
    val metaData = fsReader.getMetaData
    val t = m(metaData)
    if (pageTrigger) {
          fsReader.skip(start)
        }
        count = start
    while (fsReader.hasNext && ifContinueRead) {
        r(shuffler(fsReader.getRecord))
        totalLine += 1
      count += 1
    }
    t
  }

  def write[K <: MetaData, V <: Record](fsWriter: FsWriter[K, V]): Unit = {
    `while`(fsWriter.addMetaData, fsWriter.addRecord)
  }

  def collect(): Pair[Object, util.ArrayList[Array[String]]] = {
    val record = new util.ArrayList[Array[String]]
    val metaData = `while`(collectMetaData, r => record.add(collectRecord(r)))
    new Pair(metaData, record)
  }

  def collectRecord(record: Record): Array[String] = {
    record match {
      case t: TableRecord => t.row.map(_.toString)
      case l: LineRecord => Array(l.getLine)
    }
  }

  def collectMetaData(metaData: MetaData): Object = {
    //script/text ,tableResultset,lineResultSet
    metaData match {
      case s: ScriptMetaData => VariableParser.getMap(s.getMetaData)
      case l: LineMetaData => l.getMetaData
      case t: TableMetaData => t.columns.map(ColumnToMap)
    }
  }

  private def ColumnToMap(column: Column): java.util.Map[String, String] = {
    Map[String, String]("columnName" -> column.columnName, "comment" -> column.comment, "dataType" -> column.dataType.typeName)
  }

  //如果不分页,则一直读,如果分页,则 count需要小于count
  def ifContinueRead: Boolean = !pageTrigger || count <= end

  def ifStartRead: Boolean = !pageTrigger || count >= start

  override def close(): Unit = IOUtils.closeQuietly(fsReader)

}
