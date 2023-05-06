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

package org.apache.linkis.storage.excel;

import org.apache.poi.hssf.record.BoundSheetRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class RowToCsvDeal implements IExcelRowDeal {

  private Map<String, Object> params;
  private List<String> sheetNames;
  private OutputStream outputStream;
  private Boolean hasHeader;
  private Boolean fisrtRow = true;

  public void init(Boolean hasHeader, List<String> sheetNames, OutputStream outputStream) {
    this.hasHeader = hasHeader;
    this.sheetNames = sheetNames;
    this.outputStream = outputStream;
  }

  @Override
  public void dealRow(
      BoundSheetRecord[] orderedBSRs, int sheetIndex, int curRow, List<String> rowlist) {
    String sheetName = orderedBSRs[sheetIndex].getSheetname();
    if (sheetNames == null || sheetNames.isEmpty() || sheetNames.contains(sheetName)) {
      if (!(curRow == 0 && hasHeader)) {
        try {
          if (fisrtRow) {
            fisrtRow = false;
          } else {
            outputStream.write("\n".getBytes());
          }
          int len = rowlist.size();
          for (int i = 0; i < len; i++) {
            outputStream.write(rowlist.get(i).replaceAll("\n|\t", " ").getBytes("utf-8"));
            if (i < len - 1) {
              outputStream.write("\t".getBytes());
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
