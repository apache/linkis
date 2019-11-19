package com.webank.wedatasphere.linkis.filesystem.bml

import java.io.{ByteArrayInputStream, IOException, InputStream}

import com.webank.wedatasphere.linkis.common.io.{FsWriter, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.script.{Compaction, ScriptMetaData, ScriptRecord}


class BMLScriptWriter private (private val fileName: String) extends FsWriter {

  private val stringBuilder = new StringBuilder

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    val compactions = Compaction.listCompactions().filter(p => p.belongTo(fileName.substring(fileName.lastIndexOf(".")+1)))
    if (compactions.length > 0) {
      metaData.asInstanceOf[ScriptMetaData].getMetaData.map(compactions(0).compact).foreach(f => stringBuilder.append(f + "\n"))
    }
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    val scriptRecord = record.asInstanceOf[ScriptRecord]
    stringBuilder.append(scriptRecord.getLine)
  }

  def getInputStream():InputStream={
    new ByteArrayInputStream(stringBuilder.toString().getBytes("utf-8"))
  }

  override def close(): Unit = ???

  override def flush(): Unit = ???
}

object BMLScriptWriter{
  def getBMLScriptWriter(fileName:String) = new BMLScriptWriter(fileName)
}
