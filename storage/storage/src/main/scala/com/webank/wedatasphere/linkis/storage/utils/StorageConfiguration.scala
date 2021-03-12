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

package com.webank.wedatasphere.linkis.storage.utils

import com.webank.wedatasphere.linkis.common.conf.{ByteType, CommonVars}

/**
  * Created by johnnwang on 10/15/18.
  */
object StorageConfiguration {

  val PROXY_USER = CommonVars("wds.linkis.storage.proxy.user", "${UM}")

  val STORAGE_ROOT_USER = CommonVars("wds.linkis.storage.root.user", "hadoop")

  val HDFS_ROOT_USER = CommonVars("wds.linkis.storage.hdfs.root.user", "hadoop")

  val LOCAL_ROOT_USER = CommonVars("wds.linkis.storage.local.root.user", "root")

  val STORAGE_USER_GROUP = CommonVars("wds.linkis.storage.fileSystem.group", "bdap")

  val STORAGE_RS_FILE_TYPE = CommonVars("wds.linkis.storage.rs.file.type", "utf-8")

  val STORAGE_RS_FILE_SUFFIX = CommonVars("wds.linkis.storage.rs.file.suffix", ".dolphin")

  val ResultTypes = List( "%TEXT","%TABLE", "%HTML", "%IMG", "%ANGULAR", "%SVG")

  val STORAGE_RESULT_SET_PACKAGE = CommonVars("wds.linkis.storage.result.set.package", "com.webank.wedatasphere.linkis.storage.resultset")
  val STORAGE_RESULT_SET_CLASSES = CommonVars("wds.linkis.storage.result.set.classes", "txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet")

  val STORAGE_BUILD_FS_CLASSES = CommonVars("wds.linkis.storage.build.fs.classes", "com.webank.wedatasphere.linkis.storage.factory.impl.BuildHDFSFileSystem,com.webank.wedatasphere.linkis.storage.factory.impl.BuildLocalFileSystem")

  val IS_SHARE_NODE = CommonVars("wds.linkis.storage.is.share.node", true)

  val IO_USER = CommonVars("wds.linkis.storage.io.user", "root")
  val IO_FS_EXPIRE_TIME = CommonVars("wds.linkis.storage.io.fs.num", 1000*60*10)
  val IO_PROXY_READ_FETCH_SIZE  = CommonVars("wds.linkis.storage.io.read.fetch.size", new ByteType("100k"))
  val IO_PROXY_WRITE_CACHE_SIZE  = CommonVars("wds.linkis.storage.io.write.cache.size", new ByteType("64k"))

  val IO_DEFAULT_CREATOR = CommonVars("wds.linkis.storage.io.default.creator", "ujes")
  val IO_FS_RE_INIT = CommonVars("wds.linkis.storage.io.fs.re.init", "re-init")

  val IO_INIT_RETRY_LIMIT = CommonVars("wds.linkis.storage.io.init.retry.limit", 10)

  val STORAGE_HDFS_GROUP = CommonVars("wds.linkis.storage.fileSystem.hdfs.group", "hadoop")

  val DOUBLE_FRACTION_LEN = CommonVars[Int]("wds.linkis.double.fraction.length", 30)
}
