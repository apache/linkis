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

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;

public class StorageMultiExcelWriter extends StorageExcelWriter {

  private int sheetIndex = 0;

  public StorageMultiExcelWriter(OutputStream outputStream, boolean autoFormat) {
    super(null, null, null, outputStream, autoFormat);
  }

  @Override
  public void init() {
    if (workBook == null) {
      workBook = new SXSSFWorkbook();
    }
    // 1.让表自适应列宽
    if (sheet != null) {
      sheet.trackAllColumnsForAutoSizing();
      for (int i = 0; i <= columnCounter; i++) {
        sheet.autoSizeColumn(i);
      }
    }
    // 2.重置参数
    // 2.1 columnCounter 归0
    columnCounter = 0;
    // 2.2 创建新sheet
    sheet = workBook.createSheet("resultset" + sheetIndex);
    // 2.3 sheetIndex自增
    sheetIndex++;
    // 2.4 types 置空
    types = null;
    // 2.5 rowPoint 归0 记录行数
    rowPoint = 0;
    // 2.6 styles 清空
    styles.clear();
  }
}
