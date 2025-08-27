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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.github.pjfanning.xlsx.StreamingReader;

public class XlsxUtils {

  public static List<List<String>> getBasicInfo(InputStream inputStream, File file)
      throws Exception {
    try {
      List<List<String>> res = new ArrayList<>();
      Workbook wb = null;
      if (inputStream != null) {
        wb =
            StreamingReader.builder()
                // number of rows to keep in memory (defaults to 10)
                .rowCacheSize(2)
                .open(inputStream);
      } else {
        wb =
            StreamingReader.builder()
                // number of rows to keep in memory (defaults to 10)
                .rowCacheSize(2)
                .open(file);
      }
      List<String> sheetNames = new ArrayList<>();
      for (Sheet sheet : wb) {
        sheetNames.add(sheet.getSheetName());
      }

      Sheet sheet = wb.getSheetAt(0);
      Iterator<Row> iterator = sheet.iterator();
      Row row = null;
      while (iterator.hasNext() && row == null) {
        row = iterator.next();
      }

      if (row == null) {
        throw new Exception("The incoming Excel file is empty(传入的Excel文件为空)");
      }

      List<String> values = new ArrayList<>();
      for (Cell cell : row) {
        values.add(cell.getStringCellValue());
      }
      res.add(sheetNames);
      res.add(values);
      return res;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  public static Map<String, List<Map<String, String>>> getAllSheetInfo(
      InputStream inputStream, File file, Boolean hasHeader) throws IOException {
    try {
      Workbook wb = null;
      if (inputStream != null) {
        wb =
            StreamingReader.builder()
                // number of rows to keep in memory (defaults to 10)
                .rowCacheSize(2)
                .open(inputStream);
      } else {
        wb =
            StreamingReader.builder()
                // number of rows to keep in memory (defaults to 10)
                .rowCacheSize(2)
                .open(file);
      }
      Map<String, List<Map<String, String>>> res = new LinkedHashMap<>(wb.getNumberOfSheets());
      for (Sheet sheet : wb) {
        Iterator<Row> iterator = sheet.iterator();
        Row row = null;
        while (iterator.hasNext() && row == null) {
          row = iterator.next();
        }
        List<Map<String, String>> rowList = new ArrayList<>();
        if (row == null) {
          res.put(sheet.getSheetName(), rowList);
          continue;
        }
        int cellIdx = 0;
        for (Cell cell : row) {
          Map<String, String> item = new LinkedHashMap<>();
          if (hasHeader) {
            item.put(cell.getStringCellValue(), "string");
          } else {
            item.put("col_" + (cellIdx + 1), "string");
          }
          cellIdx++;
          rowList.add(item);
        }
        res.put(sheet.getSheetName(), rowList);
      }
      return res;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }
}
