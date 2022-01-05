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
 
package org.apache.linkis.storage.csv

import java.io._

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils


class StorageCSVWriter(val charset: String, val separator: String, val outputStream: OutputStream) extends CSVFsWriter with Logging {

  private val delimiter = separator match {
    case "," => ','
    case _ => '\t'
  }

  private val buffer: StringBuilder = new StringBuilder(50000)

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val head = metaData.asInstanceOf[TableMetaData].columns.map(_.columnName)
    write(head)
    //IOUtils.write(compact(head).getBytes(charset),outputStream)
  }

  private def compact(row: Array[String]): String = {
    val tmp = row.foldLeft("")((l, r) => l + delimiter + r)
    tmp.substring(1, tmp.length) + "\n"
  }

  private def write(row: Array[String]) = {
    val cotent: String = compact(row)
    if (buffer.length + cotent.length > 49500) {
      IOUtils.write(buffer.toString().getBytes(charset), outputStream)
      buffer.clear()
    }
    buffer.append(cotent)
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val body = record.asInstanceOf[TableRecord].row.map(_.toString) //read时候进行null替换等等
    write(body)
    //IOUtils.write(compact(body).getBytes(charset),outputStream)
  }

  override def flush(): Unit = {
    IOUtils.write(buffer.toString().getBytes(charset), outputStream)
    buffer.clear()
  }

  override def close(): Unit = {
    flush()
    IOUtils.closeQuietly(outputStream)
  }

}
