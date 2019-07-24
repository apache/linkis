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
import com.webank.wedatasphere.linkis.filesystem.util.FsUtil
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import com.webank.wedatasphere.linkis.storage.script.{ScriptFsReader, ScriptMetaData, ScriptRecord}

/**
  * Created by johnnwang on 2019/4/16.
  */
class TxtFileReader extends FileReader {
  override protected var fsReader: FsReader[MetaData, Record] = _

  def this(fileSystem: FileSystem, fsPath: FsPath, params: util.HashMap[String, String]) = {
    this()
    init(fileSystem, fsPath, params)
  }

  override def getHead: util.HashMap[String, Object] = {
    val variables = fsReader.getMetaData.asInstanceOf[ScriptMetaData].getMetaData
    import scala.collection.JavaConversions._
    val map = new util.HashMap[String, Object]
    map += "params" -> FsUtil.getParams(variables)
    map += "type" -> kind
    map
  }

  override def getBody: util.ArrayList[util.ArrayList[String]] = {
    val body = new util.ArrayList[util.ArrayList[String]]
    val bodyChild = new util.ArrayList[String]()
    while (fsReader.hasNext) bodyChild.add(fsReader.getRecord.asInstanceOf[ScriptRecord].getLine)
    fsReader.close()
    body.add(bodyChild)
    body
  }

  override def init(fileSystem: FileSystem, fsPath: FsPath, params: util.HashMap[String, String]): Unit = {
    val charset = params.getOrDefault("charset", "utf-8")
    this.fsReader = ScriptFsReader.getScriptFsReader(fsPath, charset, fileSystem.read(fsPath)).asInstanceOf[FsReader[MetaData, Record]]
    //this.fsReader = ScriptFsReader.getScriptFsReader(fsPath, charset, new FileInputStream(fsPath.getPath)).asInstanceOf[FsReader[MetaData, Record]]
  }

  override protected var kind: String = "script/text"
}
