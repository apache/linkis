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

package org.apache.linkis.storage.source;

import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.resultset.table.TableRecord;
import org.apache.linkis.storage.utils.StorageUtils;

import java.util.Arrays;

public class ResultsetFileSource extends AbstractFileSource {

  public ResultsetFileSource(FileSplit[] fileSplits) {
    super(fileSplits);
    shuffle(
        record -> {
          if (record instanceof TableRecord) {
            TableRecord tableRecord = (TableRecord) record;
            String nullValue = getParams().getOrDefault("nullValue", "NULL");
            return new TableRecord(
                Arrays.stream(tableRecord.row)
                    .map(
                        r -> {
                          if (r == null || r.equals("NULL")) {
                            if (nullValue.equals(Dolphin.LINKIS_NULL)) {
                              return r;
                            } else {
                              return nullValue;
                            }
                          } else if (r.equals("")) {
                            String emptyValue = getParams().getOrDefault("nullValue", "");
                            if (emptyValue.equals(Dolphin.LINKIS_NULL)) {
                              return "";
                            } else {
                              return nullValue;
                            }
                          } else if (r instanceof Double) {
                            return StorageUtils.doubleToString((Double) r);
                          } else {
                            return r;
                          }
                        })
                    .toArray());
          } else {
            return record;
          }
        });
  }
}
