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

package org.apache.linkis.storage.resultset.io;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSerializer;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.codec.binary.Base64;

public class IOResultSerializer extends ResultSerializer {

  @Override
  public byte[] metaDataToBytes(MetaData metaData) {
    IOMetaData ioMetaData = (IOMetaData) metaData;
    return lineToBytes(ioMetaData.off + Dolphin.COL_SPLIT + ioMetaData.len);
  }

  @Override
  public byte[] recordToBytes(Record record) {
    IORecord ioRecord = (IORecord) record;
    return lineToBytes(Base64.encodeBase64String(ioRecord.value));
  }

  private byte[] lineToBytes(String value) {
    byte[] bytes = value == null ? Dolphin.NULL_BYTES : Dolphin.getBytes(value);
    byte[] intBytes = Dolphin.getIntBytes(bytes.length);
    return StorageUtils.mergeByteArrays(intBytes, bytes);
  }
}
