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

import java.util.ArrayList;
import java.util.List;

class FirstRowDeal implements IExcelRowDeal {

  private List<String> sheetNames = new ArrayList<>();
  private List<String> row;

  public List<String> getSheetNames() {
    return sheetNames;
  }

  public void setSheetNames(List<String> sheetNames) {
    this.sheetNames = sheetNames;
  }

  public List<String> getRow() {
    return row;
  }

  public void setRow(List<String> row) {
    this.row = row;
  }

  @Override
  public void dealRow(
      BoundSheetRecord[] orderedBSRs, int sheetIndex, int curRow, List<String> rowlist) {
    for (BoundSheetRecord record : orderedBSRs) {
      sheetNames.add(record.getSheetname());
    }
    row = rowlist;
    throw new ExcelAnalysisException("Finished to deal first row");
  }
}
