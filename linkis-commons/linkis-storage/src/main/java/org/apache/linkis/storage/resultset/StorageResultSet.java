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

package org.apache.linkis.storage.resultset;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.resultset.table.TableResultSet;
import org.apache.linkis.storage.utils.StorageConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StorageResultSet<K extends MetaData, V extends Record>
    implements ResultSet<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(StorageResultSet.class);

  private byte[] resultHeaderBytes = null;

  {
    byte[] arr2 = Dolphin.getIntBytes(Integer.parseInt(resultSetType()));
    byte[] mergedArray = new byte[Dolphin.MAGIC_BYTES.length + arr2.length];
    System.arraycopy(Dolphin.MAGIC_BYTES, 0, mergedArray, 0, Dolphin.MAGIC_BYTES.length);
    System.arraycopy(arr2, 0, mergedArray, Dolphin.MAGIC_BYTES.length, arr2.length);
    resultHeaderBytes = mergedArray;
  }

  @Override
  public String charset() {
    return StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue();
  }

  @Override
  public FsPath getResultSetPath(FsPath parentDir, String fileName) {
    String engineResultType = LinkisStorageConf.ENGINE_RESULT_TYPE;
    String fileSuffix = Dolphin.DOLPHIN_FILE_SUFFIX;
    if (engineResultType.equals(LinkisStorageConf.PARQUET) && this instanceof TableResultSet) {
      fileSuffix = LinkisStorageConf.PARQUET_FILE_SUFFIX;
    } else if (engineResultType.equals(LinkisStorageConf.ORC) && this instanceof TableResultSet) {
      fileSuffix = LinkisStorageConf.ORC_FILE_SUFFIX;
    }
    final String path =
        parentDir.getPath().endsWith("/")
            ? parentDir.getUriString() + fileName + fileSuffix
            : parentDir.getUriString() + "/" + fileName + fileSuffix;
    logger.info("Get result set path: {}", path);
    return new FsPath(path);
  }

  @Override
  public byte[] getResultSetHeader() {
    return resultHeaderBytes;
  }

  @Override
  public boolean belongToPath(String path) {
    return path.endsWith(Dolphin.DOLPHIN_FILE_SUFFIX);
  }

  @Override
  public boolean belongToResultSet(String content) {
    try {
      return Dolphin.getType(content).equals(resultSetType());
    } catch (Exception e) {
      logger.info("Wrong result set: ", e);
      return false;
    }
  }
}
