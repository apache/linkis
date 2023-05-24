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

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.storage.LineMetaData;
import org.apache.linkis.storage.LineRecord;

import java.io.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StorageResultSetWriterFactoryTest {

  @Test
  void testResultSetWrite() throws IOException {
    // storage write
    ResultSet<? extends MetaData, ? extends Record> resultSetByType =
        ResultSetFactory.getInstance().getResultSetByType(ResultSetFactory.TEXT_TYPE);

    org.apache.linkis.common.io.resultset.ResultSetWriter<? extends MetaData, ? extends Record>
        writer = ResultSetWriterFactory.getResultSetWriter(resultSetByType, 100L, null);

    String value = "value";
    LineMetaData metaData = new LineMetaData(null);
    LineRecord record = new LineRecord(value);
    writer.addMetaData(metaData);
    writer.addRecord(record);
    writer.flush();
    writer.close();
    String res = writer.toString();
    writer.close();
    Assertions.assertEquals("dolphin00000000010000000004NULL0000000005value", res);
  }
}
