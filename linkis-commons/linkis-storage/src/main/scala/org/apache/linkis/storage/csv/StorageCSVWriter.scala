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

package org.apache.linkis.storage.csv

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.domain.DataType
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io._

class StorageCSVWriter(
    val charset: String,
    val separator: String,
    val quoteRetouchEnable: Boolean,
    val outputStream: OutputStream
) extends CSVFsWriter
    with Logging {

  private val delimiter = separator match {
    case separ if StringUtils.isNotEmpty(separ) => separ
    case _ => '\t'
  }

  private val buffer: StringBuilder = new StringBuilder(50000)

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val head = metaData.asInstanceOf[TableMetaData].columns.map(_.columnName)
    write(head)
  }

  private def compact(row: Array[String]): String = {
    val quotationMarks: String = "\""
    def decorateValue(v: String): String = {
      if (StringUtils.isBlank(v)) v
      else {
        if (quoteRetouchEnable) {
          s"$quotationMarks${v.replaceAll(quotationMarks, "")}$quotationMarks"
        } else v
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("delimiter:" + delimiter.toString)
    }

    row.map(x => decorateValue(x)).toList.mkString(delimiter.toString) + "\n"
  }

  private def write(row: Array[String]) = {
    val content: String = compact(row)
    if (buffer.length + content.length > 49500) {
      IOUtils.write(buffer.toString().getBytes(charset), outputStream)
      buffer.clear()
    }
    buffer.append(content)
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val body = record.asInstanceOf[TableRecord].row.map(DataType.valueToString)
    write(body)
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
