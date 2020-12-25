/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.source

import java.util

import com.webank.wedatasphere.linkis.common.io.{FsReader, FsWriter, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.script.{ScriptMetaData, VariableParser}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.math3.util.Pair

import scala.collection.JavaConversions._

/**
  * Created by johnnwang on 2020/1/15.
  */
abstract class AbstractFileSource extends FileSource {

  protected var start: Int = 0

  protected var end: Int = -1

  protected var count: Int = 0

  protected var totalLine = 0

  protected var shuffler: Record => Record = r => r

  protected var pageTrigger: Boolean = false

  protected var params: util.Map[String, String] = new util.HashMap[String, String]

  protected var fsReader: FsReader[_ <: MetaData, _ <: Record] = _

  protected var `type`: String = "script/text"

  def setType(`type`: String): AbstractFileSource = {
    params += "type" -> `type`
    this.`type` = `type`
    this
  }

  override def shuffle(s: Record => Record): AbstractFileSource = {
    this.shuffler = s
    this
  }

  override def page(page: Int, pageSize: Int): AbstractFileSource = {
    if (pageTrigger) return this
    start = (page - 1) * pageSize
    end = pageSize * page - 1
    pageTrigger = true
    this
  }

  override def addParams(params: util.Map[String, String]): AbstractFileSource = {
    this.params.putAll(params)
    this
  }

  override def addParams(key: String, value: String): FileSource = {
    this.params += key -> value
    this
  }

  override def getParams(): util.Map[String, String] = this.params

  def setFsReader[K <: MetaData, V <: Record](fsReader: FsReader[K, V]): AbstractFileSource = {
    this.fsReader = fsReader
    this
  }

  override def write[K <: MetaData, V <: Record](fsWriter: FsWriter[K, V]): Unit = {
    `while`(fsWriter.addMetaData, fsWriter.addRecord)
  }

  def `while`[M](m: MetaData => M, r: Record => Unit): M = {
    val metaData = fsReader.getMetaData
    val t = m(metaData)
    while (fsReader.hasNext && ifContinueRead) {
      if (ifStartRead) {
        r(shuffler(fsReader.getRecord))
        totalLine += 1
      }
      count += 1
    }
    t
  }

  override def close(): Unit = IOUtils.closeQuietly(this.fsReader)

  def collectRecord(record: Record): Array[String] = {
    record match {
      case t: TableRecord => t.row.map(_.toString)
      case l: LineRecord => Array(l.getLine)
    }
  }

  override def collect(): Pair[Object, util.ArrayList[Array[String]]] = {
    val record = new util.ArrayList[Array[String]]
    val metaData = `while`(collectMetaData, r => record.add(collectRecord(r)))
    this.params += "totalLine" -> totalLine.toString
    new Pair(metaData, record)
  }

  //如果不分页,则一直读,如果分页,则 count需要小于count
  def ifContinueRead: Boolean = !pageTrigger || count <= end

  def ifStartRead: Boolean = !pageTrigger || count >= start

  def collectMetaData(metaData: MetaData): Object = {
    //script/text ,tableResultset,lineResultSet
    metaData match {
      case s: ScriptMetaData => VariableParser.getMap(s.getMetaData)
      case l: LineMetaData => l.getMetaData
      case t: TableMetaData => t.columns.map(_.toString)
    }
  }

}
