/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.storage.utils

import org.apache.linkis.common.conf.{ByteType, CommonVars}


object StorageConfiguration {

  val PROXY_USER = CommonVars("wds.linkis.storage.proxy.user", "${UM}")

  val STORAGE_ROOT_USER = CommonVars("wds.linkis.storage.root.user", "hadoop")

  val HDFS_ROOT_USER = CommonVars("wds.linkis.storage.hdfs.root.user", "hadoop")

  val LOCAL_ROOT_USER = CommonVars("wds.linkis.storage.local.root.user", "root")

  val STORAGE_USER_GROUP = CommonVars("wds.linkis.storage.fileSystem.group", "bdap")

  val STORAGE_RS_FILE_TYPE = CommonVars("wds.linkis.storage.rs.file.type", "utf-8")

  val STORAGE_RS_FILE_SUFFIX = CommonVars("wds.linkis.storage.rs.file.suffix", ".dolphin")

  val ResultTypes = List( "%TEXT","%TABLE", "%HTML", "%IMG", "%ANGULAR", "%SVG")

  val STORAGE_RESULT_SET_PACKAGE = CommonVars("wds.linkis.storage.result.set.package", "org.apache.linkis.storage.resultset")
  val STORAGE_RESULT_SET_CLASSES = CommonVars("wds.linkis.storage.result.set.classes", "txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet")

  val STORAGE_BUILD_FS_CLASSES = CommonVars("wds.linkis.storage.build.fs.classes", "org.apache.linkis.storage.factory.impl.BuildHDFSFileSystem,org.apache.linkis.storage.factory.impl.BuildLocalFileSystem")

  val IS_SHARE_NODE = CommonVars("wds.linkis.storage.is.share.node", true)


  val ENABLE_IO_PROXY = CommonVars("wds.linkis.storage.enable.io.proxy", false)

  val IO_USER = CommonVars("wds.linkis.storage.io.user", "root")
  val IO_FS_EXPIRE_TIME = CommonVars("wds.linkis.storage.io.fs.num", 1000*60*10)
  val IO_PROXY_READ_FETCH_SIZE = CommonVars("wds.linkis.storage.io.read.fetch.size", new ByteType("100k"))
  val IO_PROXY_WRITE_CACHE_SIZE = CommonVars("wds.linkis.storage.io.write.cache.size", new ByteType("64k"))

  val IO_DEFAULT_CREATOR = CommonVars("wds.linkis.storage.io.default.creator", "IDE")
  val IO_FS_RE_INIT = CommonVars("wds.linkis.storage.io.fs.re.init", "re-init")

  val IO_INIT_RETRY_LIMIT = CommonVars("wds.linkis.storage.io.init.retry.limit", 10)

  val STORAGE_HDFS_GROUP = CommonVars("wds.linkis.storage.fileSystem.hdfs.group", "hadoop")

  val DOUBLE_FRACTION_LEN = CommonVars[Int]("wds.linkis.double.fraction.length", 30)

  val HDFS_PATH_PREFIX_CHECK_ON = CommonVars[Boolean]("wds.linkis.storage.hdfs.prefix_check.enable", true)

  val HDFS_PATH_PREFIX_REMOVE = CommonVars[Boolean]("wds.linkis.storage.hdfs.prefxi.remove", true)

  val CODE_TYPE_AND_RUN_TYPE_RELATION = CommonVars("wds.linkis.storage.codeType.runType.relation", "sql=>hql|sql|jdbc|hive|psql|sh|shell,python=>python|py,java=>java,scala=>scala")

  val RUN_TYPE_SQL = "sql"
  val RUN_TYPE_PYTHON = "python"
  val RUN_TYPE_JAVA = "java"
  val RUN_TYPE_SCALA = "scala"

  def getCodeTypeAndRunTypeRelationMap: Map[String, List[String]] = {
    if (CODE_TYPE_AND_RUN_TYPE_RELATION.getValue == null || "".equals(CODE_TYPE_AND_RUN_TYPE_RELATION.getValue)) {
      return Map()
    }

    CODE_TYPE_AND_RUN_TYPE_RELATION.getValue.split(",")
      .filter(x => x != null && !"".equals(x))
      .map(x => {
        val confArr = x.split("=>")
        if (confArr.length == 2) (confArr(0), for (x <- confArr(1).split("\\|").toList) yield x.trim) else null
      }).filter(x => x != null).toMap
  }

  def getSuffixBelongToRunTypeOrNot(suffix: String, runType: String): Boolean = {
    val codeTypeAndRunTypeRelationMap = getCodeTypeAndRunTypeRelationMap
    if (codeTypeAndRunTypeRelationMap.isEmpty) return false
    val suffixListOfRunType = codeTypeAndRunTypeRelationMap.getOrElse(runType, List())
    if (suffixListOfRunType.isEmpty) return false
    if (suffixListOfRunType.contains(suffix)) return true
    false
  }
}
