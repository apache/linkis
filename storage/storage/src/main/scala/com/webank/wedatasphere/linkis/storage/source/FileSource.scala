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

package com.webank.wedatasphere.linkis.storage.source

import java.io.{Closeable, InputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io._
import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.linkis.storage.script.ScriptFsReader
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration
import org.apache.commons.math3.util.Pair

/**
 * Created by johnnwang on 2020/1/15.
 */
trait FileSource extends Closeable {

  def shuffle(s: Record => Record): FileSource

  def page(page: Int, pageSize: Int): FileSource

  def collect(): Pair[Object, util.ArrayList[Array[String]]]

  def write[K <: MetaData, V <: Record](fsWriter: FsWriter[K, V]): Unit

  def addParams(params: util.Map[String, String]): FileSource

  def addParams(key: String, value: String): FileSource

  def getParams(): util.Map[String, String]

}

object FileSource {

  private val fileType = Array("dolphin", "sql", "scala", "py", "hql", "python", "out", "log", "text", "sh", "jdbc", "mlsql")

  private val suffixPredicate = (path: String, suffix: String) => path.endsWith(s".$suffix")

  def isResultSet(path: String): Boolean = {
    suffixPredicate(path, fileType.head)
  }

  def isTableResultSet(fileSource: FileSource): Boolean = {
    ResultSetFactory.TABLE_TYPE.equals(fileSource.getParams().get("type"))
  }

  def create(fsPath: FsPath, fs: Fs): FileSource = {
    create(fsPath, fs.read(fsPath))
  }

  def create(fsPath: FsPath, is: InputStream): FileSource = {
    canRead(fsPath.getPath)
    if (isResultSet(fsPath.getPath)) {
      val resultset = ResultSetFactory.getInstance.getResultSetByPath(fsPath)
      val resultsetReader = ResultSetReader.getResultSetReader(resultset, is)
      new ResultsetFileSource().setFsReader(resultsetReader).setType(resultset.resultSetType())
    } else {
      val scriptFsReader = ScriptFsReader.getScriptFsReader(fsPath, StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue, is)
      new TextFileSource().setFsReader(scriptFsReader)
    }
  }

  private def canRead(path: String) = {
    if (!fileType.exists(suffixPredicate(path, _))) throw new StorageErrorException(54001, "不支持打开的文件类型")
  }

}


