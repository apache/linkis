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

package com.webank.wedatasphere.linkis.storage.script.writer

import java.io.{IOException, OutputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.script.{Compaction, ScriptFsWriter, ScriptMetaData, ScriptRecord}
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils
import org.apache.commons.io.IOUtils

/**
  * Created by johnnwang on 2018/10/23.
  */
class StorageScriptFsWriter(pathP: FsPath, charsetP: String, outputStreamP: OutputStream) extends ScriptFsWriter {
  override val path: FsPath = pathP
  override val charset: String = charsetP
  override val outputStream: OutputStream = outputStreamP

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val compactions = Compaction.listCompactions().filter(p => p.belongTo(StorageUtils.pathToSuffix(path.getPath)))
    val metadataLineJ = new util.ArrayList[String]()
    if (compactions.length > 0) {
      metaData.asInstanceOf[ScriptMetaData].getMetaData.map(compactions(0).compact).foreach(metadataLineJ.add)
      IOUtils.writeLines(metadataLineJ, "\n", outputStream, charset)
    }
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val scriptRecord = record.asInstanceOf[ScriptRecord]
    IOUtils.write(scriptRecord.getLine, outputStream, charset)
  }

  override def close(): Unit = {
    StorageUtils.close(outputStream)
  }

  override def flush(): Unit = if (outputStream != null) outputStream.flush()

}

