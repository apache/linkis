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

package com.webank.wedatasphere.linkis.storage.csv

import java.io._
import java.util
import java.util.Collections

import com.webank.wedatasphere.linkis.common.io.{MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.storage.domain.DataType
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}

/**
  * Created by johnnwang on 2018/11/12.
  */
class StorageCSVWriter(charsetP: String, separatorP: String) extends CSVFsWriter with Logging {

  override val charset: String = charsetP
  override val separator: String = separatorP

  private val delimiter = separator match {
    case "," => ','
    case _ =>'\t'
  }

  private val cSVWriter = new util.ArrayList[InputStream]()
  private var stream: SequenceInputStream = _
  private val buffer: StringBuilder = new StringBuilder(40000)
  private var counter: Int = _

  override def setIsLastRow(value: Boolean): Unit = {}

  def collectionInputStream(content: Array[String]): Unit = {
    content.foreach(f => counter += f.length)
    counter += content.size
    if (counter >= 40000) {
      cSVWriter.add(new ByteArrayInputStream(buffer.toString().getBytes(charset)))
      buffer.clear()
      content.indices.map {
        case 0 => content(0)
        case i => delimiter + content(i)
      }.foreach(buffer.append)
      buffer.append("\n")
      counter = buffer.length
    } else {
      content.indices.map {
        case 0 => content(0)
        case i => delimiter + content(i)
      }.foreach(buffer.append)
      buffer.append("\n")
      counter = buffer.length
    }
  }

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val head = metaData.asInstanceOf[TableMetaData].columns.map(_.columnName)
    collectionInputStream(head)
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val body = record.asInstanceOf[TableRecord].row.map(f => if (f != null) f.toString else DataType.NULL_VALUE)
    collectionInputStream(body)
  }

  override def flush(): Unit = {}

  override def close(): Unit = {
    if (stream != null) stream.close()
  }

  override def getCSVStream: InputStream = {
    cSVWriter.add(new ByteArrayInputStream(buffer.toString().getBytes(charset)))
    buffer.clear()
    counter = 0 //Subsequent move to flush(后续挪到flush)
    stream = new SequenceInputStream(Collections.enumeration(cSVWriter))
    stream
  }
}
