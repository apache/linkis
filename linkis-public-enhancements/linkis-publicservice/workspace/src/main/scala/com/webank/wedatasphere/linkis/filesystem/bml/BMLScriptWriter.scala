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
package com.webank.wedatasphere.linkis.filesystem.bml

import java.io.{ByteArrayInputStream, IOException, InputStream}

import com.webank.wedatasphere.linkis.common.io.{FsWriter, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.script.{Compaction, ScriptMetaData, ScriptRecord}
/**
  * Created by patinousward
  */
class BMLScriptWriter private (private val fileName: String) extends FsWriter {

  private val stringBuilder = new StringBuilder

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val compactions = Compaction.listCompactions().filter(p => p.belongTo(fileName.substring(fileName.lastIndexOf(".")+1)))
    if (compactions.length > 0) metaData.asInstanceOf[ScriptMetaData].getMetaData.map(compactions(0).compact).foreach(f => stringBuilder.append(f + "\n"))
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val scriptRecord = record.asInstanceOf[ScriptRecord]
    stringBuilder.append(scriptRecord.getLine)
  }

  def getInputStream():InputStream= new ByteArrayInputStream(stringBuilder.toString().getBytes("utf-8"))

  override def close(): Unit = ???

  override def flush(): Unit = ???
}

object BMLScriptWriter{
  def getBMLScriptWriter(fileName:String) = new BMLScriptWriter(fileName)
}
