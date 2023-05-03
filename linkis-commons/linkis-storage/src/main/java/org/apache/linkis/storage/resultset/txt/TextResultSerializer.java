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

package org.apache.linkis.storage.resultset.txt;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSerializer;
import org.apache.linkis.storage.LineMetaData;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.utils.StorageUtils;

public class TextResultSerializer extends ResultSerializer {

  @Override
  public byte[] metaDataToBytes(MetaData metaData) {
    if (metaData == null) {
      return lineToBytes(null);
    } else {
      LineMetaData textMetaData = (LineMetaData) metaData;
      return lineToBytes(textMetaData.getMetaData());
    }
  }

  @Override
  public byte[] recordToBytes(Record record) {
    LineRecord textRecord = (LineRecord) record;
    return lineToBytes(textRecord.getLine());
  }

  private byte[] lineToBytes(String value) {
    byte[] bytes = (value == null) ? Dolphin.NULL_BYTES : Dolphin.getBytes(value);
    return StorageUtils.mergeByteArrays(Dolphin.getIntBytes(bytes.length), bytes);
  }
}
