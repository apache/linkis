package org.apache.linkis.storage

import org.apache.linkis.common.io.MetaData
import org.apache.linkis.storage.resultset.ResultMetaData

class LineMetaData(private var metaData: String = null) extends ResultMetaData {

  def getMetaData: String = metaData

  def setMetaData(metaData: String): Unit = {
    this.metaData = metaData
  }

  override def cloneMeta(): MetaData = new LineMetaData(metaData)
}
