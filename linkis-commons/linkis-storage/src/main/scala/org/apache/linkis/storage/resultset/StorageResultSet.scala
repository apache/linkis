/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.storage.resultset

import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSet
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.utils.StorageConfiguration

abstract class StorageResultSet[K <: MetaData, V <: Record] extends ResultSet[K, V] with Logging {

  val resultHeaderBytes = Dolphin.MAGIC_BYTES ++ Dolphin.getIntBytes(resultSetType().toInt)
  override val charset: String = StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue

  override def getResultSetPath(parentDir: FsPath, fileName: String): FsPath = {
    val path = if (parentDir.getPath.endsWith("/")) {
      parentDir.toPath + fileName + Dolphin.DOLPHIN_FILE_SUFFIX
    } else {
      parentDir.toPath + "/" + fileName + Dolphin.DOLPHIN_FILE_SUFFIX
    }
    new FsPath(path)
  }

  override def getResultSetHeader: Array[Byte] = resultHeaderBytes

  override def belongToPath(path: String): Boolean = path.endsWith(Dolphin.DOLPHIN_FILE_SUFFIX)

  override def belongToResultSet(content: String): Boolean =
    Utils.tryCatch(Dolphin.getType(content) == resultSetType()) { t =>
      logger.info("Wrong result Set: ", t)
      false
    }

}
