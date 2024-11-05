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

package org.apache.linkis.storage.script.writer

import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.LineRecord
import org.apache.linkis.storage.script.{Compaction, ScriptFsWriter, ScriptMetaData}
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import org.apache.commons.io.IOUtils
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream

import java.io.{ByteArrayInputStream, InputStream, IOException, OutputStream}
import java.util

class StorageScriptFsWriter(
    val path: FsPath,
    val charset: String,
    outputStream: OutputStream = null
) extends ScriptFsWriter
    with Logging {

  private val stringBuilder = new StringBuilder

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {

    val metadataLine = new util.ArrayList[String]()
    val compaction = getScriptCompaction()
    if (compaction != null) {

      metaData
        .asInstanceOf[ScriptMetaData]
        .getMetaData
        .map(compaction.compact)
        .foreach(metadataLine.add)
      // add annotition symbol
      if (metadataLine.size() > 0) {
        metadataLine.add(compaction.getAnnotationSymbol())
      }
      if (outputStream != null) {
        IOUtils.writeLines(metadataLine, "\n", outputStream, charset)
      } else {
        import scala.collection.JavaConverters._
        metadataLine.asScala.foreach(m => stringBuilder.append(s"$m\n"))
      }
    }

  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    // 转成LineRecord而不是TableRecord是为了兼容非Table类型的结果集写到本类中
    val scriptRecord = record.asInstanceOf[LineRecord]
    if (outputStream != null) {
      IOUtils.write(scriptRecord.getLine, outputStream, charset)
    } else {
      stringBuilder.append(scriptRecord.getLine)
    }
  }

  override def close(): Unit = {
    if (outputStream != null) {
      IOUtils.closeQuietly(outputStream)
    }
  }

  override def flush(): Unit = if (outputStream != null) {
    Utils.tryAndWarnMsg[Unit] {
      outputStream match {
        case hdfs: HdfsDataOutputStream =>
          hdfs.hflush()
        case _ =>
          outputStream.flush()
      }
    }(s"Error encounters when flush script ")
  }

  def getInputStream(): InputStream = {
    new ByteArrayInputStream(
      stringBuilder.toString().getBytes(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue)
    )
  }

  /**
   * get the script compaction according to the path(根据文件路径 获取对应的script Compaction )
   * @return
   *   Scripts Compaction
   */

  def getScriptCompaction(): Compaction = {

    val compactions = Compaction
      .listCompactions()
      .filter(p => p.belongTo(StorageUtils.pathToSuffix(path.getPath)))

    if (compactions.length > 0) {
      compactions(0)
    } else {
      null
    }
  }

}
