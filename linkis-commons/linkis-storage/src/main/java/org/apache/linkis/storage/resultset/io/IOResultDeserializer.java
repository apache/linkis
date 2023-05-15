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

import org.apache.linkis.common.io.resultset.ResultDeserializer;
import org.apache.linkis.storage.domain.Dolphin;

import org.apache.commons.codec.binary.Base64;

public class IOResultDeserializer extends ResultDeserializer<IOMetaData, IORecord> {

  @Override
  public IOMetaData createMetaData(byte[] bytes) {
    String[] values = Dolphin.getString(bytes, 0, bytes.length).split(Dolphin.COL_SPLIT);
    return new IOMetaData(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
  }

  @Override
  public IORecord createRecord(byte[] bytes) {
    return new IORecord(Base64.decodeBase64(Dolphin.getString(bytes, 0, bytes.length)));
  }
}
