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

package com.webank.wedatasphere.linkis.filesystem.reader

import java.util

import com.webank.wedatasphere.linkis.common.io.{FsPath, FsReader, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}

/**
  * Created by johnnwang on 2019/4/16.
  */
class RSReader extends FileReader {

  def this(fileSystem: FileSystem, fsPath: FsPath, params: util.HashMap[String, String]) = {
    this()
    init(fileSystem, fsPath, params)
  }

  override protected var fsReader: FsReader[MetaData, Record] = _

  private var rsType: InnerParent = _

  override def init(fileSystem: FileSystem, fsPath: FsPath, params: util.HashMap[String, String]): Unit = {
    val resultset = ResultSetFactory.getInstance.getResultSetByPath(fsPath)
    kind = resultset.resultSetType()
    fsReader = ResultSetReader.getResultSetReader(resultset, fileSystem.read(fsPath)).asInstanceOf[FsReader[MetaData, Record]]
    rsType = fsReader.getMetaData match {
      case t: TableMetaData => new InnerTableReader(t)
      case l: LineMetaData => new InnerLineReader(l)
    }
  }

  override def getHead: util.HashMap[String, Object] = rsType.getHead

  override def getBody: util.ArrayList[util.ArrayList[String]] = rsType.getBody

  class InnerParent {
    def getHead: util.HashMap[String, Object] = null

    def getBody: util.ArrayList[util.ArrayList[String]] = null
  }

  class InnerLineReader(private val metaData: MetaData) extends InnerParent {
    override def getHead: util.HashMap[String, Object] = {
      val lineMetaData = metaData.asInstanceOf[LineMetaData]
      import scala.collection.JavaConversions._
      val map = new util.HashMap[String, Object]
      map += "metadata" -> lineMetaData.getMetaData
      map += "type" -> kind
      map
    }

    override def getBody: util.ArrayList[util.ArrayList[String]] = {
      val body = new util.ArrayList[util.ArrayList[String]]
      val bodyChild = new util.ArrayList[String]
      while (fsReader.hasNext) bodyChild.add(fsReader.getRecord.asInstanceOf[LineRecord].getLine)
      fsReader.close()
      body.add(bodyChild)
      body
    }
  }

  class InnerTableReader(private val metaData: MetaData) extends InnerParent {
    override def getHead: util.HashMap[String, Object] = {
      val columns = metaData.asInstanceOf[TableMetaData].columns
      val resultsetMetaDataList = new util.ArrayList[String]()
      columns.foreach(f => resultsetMetaDataList.add(f.toString()))
      import scala.collection.JavaConversions._
      val map = new util.HashMap[String, Object]
      map += "metadata" -> resultsetMetaDataList
      map += "type" -> kind
      map
    }

    /**
      * Grab 5000 rows directly here.(这里直接先抓取5000行)
      *
      * @return
      */
    override def getBody: util.ArrayList[util.ArrayList[String]] = {
      val body = new util.ArrayList[util.ArrayList[String]]
      var row = 0
      while (fsReader.hasNext && row <= 5000) {
        val rows = fsReader.getRecord.asInstanceOf[TableRecord].row
        val bodyChild = new util.ArrayList[String]()
        rows.foreach {
          f => if (f == null) bodyChild.add("NULL") else bodyChild.add(f.toString)
        }
        body.add(bodyChild)
        row += 1
      }
      fsReader.close()
      body
    }
  }

  override protected var kind: String = _
}
