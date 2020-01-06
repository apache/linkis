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

package com.webank.wedatasphere.linkis.storage.resultset

import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.common.io.resultset.{ResultDeserializer, ResultSerializer, ResultSet}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.storage.domain.Dolphin
import com.webank.wedatasphere.linkis.storage.utils.StorageConfiguration

/**
  * Created by johnnwang on 10/20/18.
  */
abstract class StorageResultSet[K <: MetaData, V <: Record] extends ResultSet[K, V] with Logging{

  val resultHeaderBytes = Dolphin.MAGIC_BYTES ++ Dolphin.getIntBytes(resultSetType().toInt)
  override val charset: String = StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue



  override def getResultSetPath(parentDir: FsPath, fileName: String): FsPath = {
    val path = if(parentDir.getSchemaPath.endsWith("/"))
      parentDir.getSchemaPath + fileName + Dolphin.DOLPHIN_FILE_SUFFIX
    else
      parentDir.getSchemaPath + "/" + fileName  + Dolphin.DOLPHIN_FILE_SUFFIX
    new FsPath(path)
  }

  override def getResultSetHeader: Array[Byte] = resultHeaderBytes

  override def belongToPath(path: String): Boolean = path.endsWith(Dolphin.DOLPHIN_FILE_SUFFIX)

  override def belongToResultSet(content: String): Boolean = Utils.tryCatch(Dolphin.getType(content) == resultSetType()){ t =>
    info("Wrong result Set: ", t)
    false
  }

}
