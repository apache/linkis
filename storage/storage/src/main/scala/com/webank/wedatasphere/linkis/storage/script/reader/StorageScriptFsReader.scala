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

package com.webank.wedatasphere.linkis.storage.script.reader

import java.io._

import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.storage.script._
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils

import scala.collection.mutable.ArrayBuffer


/**
  * Created by johnnwang on 2018/10/23.
  */
class StorageScriptFsReader(pathP: FsPath, charsetP: String, inputStreamP: InputStream) extends ScriptFsReader {

  override val path: FsPath = pathP
  override val charset: String = charsetP

  private val inputStream: InputStream = inputStreamP
  private var inputStreamReader: InputStreamReader = _
  private var BufferedReader: BufferedReader = _

  private var metadata: ScriptMetaData = _

  private var variables: ArrayBuffer[Variable] = new ArrayBuffer[Variable]()
  private var lineText: String = _

  @scala.throws[IOException]
  override def getRecord: Record = {

    if (metadata == null) throw new IOException("Must read metadata first(必须先读取metadata)")
    val record = new ScriptRecord(lineText)
    lineText = BufferedReader.readLine()
    record
  }

  @scala.throws[IOException]
  override def getMetaData: MetaData = {
    if (metadata == null) init()
    val parser = ParserFactory.listParsers().filter(p => p.belongTo(StorageUtils.pathToSuffix(path.getPath)))
    lineText = BufferedReader.readLine()
    while (hasNext && parser.length > 0 && isMetadata(lineText, parser(0).prefix,parser(0).prefixConf)) {
      variables += parser(0).parse(lineText)
      lineText = BufferedReader.readLine()
    }
    metadata = new ScriptMetaData(variables.toArray)
    metadata
  }

  def init(): Unit = {
    inputStreamReader = new InputStreamReader(inputStream)
    BufferedReader = new BufferedReader(inputStreamReader)
  }

  @scala.throws[IOException]
  override def skip(recordNum: Int): Int = -1

  @scala.throws[IOException]
  override def getPosition: Long = -1L

  @scala.throws[IOException]
  override def hasNext: Boolean = lineText != null

  @scala.throws[IOException]
  override def available: Long = if (inputStream != null) inputStream.available() else 0L

  override def close(): Unit = {
    if (BufferedReader != null) BufferedReader.close()
    if (inputStreamReader != null) inputStreamReader.close()
    if (inputStream != null) inputStream.close()
  }

  /**
    * Determine if the read line is metadata(判断读的行是否是metadata)
    * @param line
    * @return
    */
  def isMetadata(line: String, prefix: String,prefixConf:String): Boolean = {
    val regex = ("\\s*" + prefix + "\\s*(.+)\\s*" + "=" + "\\s*(.+)\\s*").r
    line match {
      case regex(_,_) => true
      case _ => {
        val split: Array[String] = line.split("=")
        if(split.size !=2)  return false
        if (split(0).split(" ").filter(_!="").size != 4)  return false
        if(!split(0).split(" ").filter(_!="")(0).equals(prefixConf)) return  false
        true
      }
    }
  }
}




