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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Map;

import org.junit.jupiter.api.*;

/** ExcelStorageReader Tester */
public class ExcelStorageReaderTest {

  @Autowired private ExcelStorageReader excelStorageReader;

  @BeforeEach
  @DisplayName("Each unit test method is executed once before execution")
  public void before() throws Exception {}

  @AfterEach
  @DisplayName("Each unit test method is executed once before execution")
  public void after() throws Exception {}

  private static InputStream createExcelAndGetInputStream(int type) throws IOException {
    Workbook workbook = new HSSFWorkbook();
    if (type == 1) {
      workbook = new XSSFWorkbook();
    }
    // 创建一个新的工作簿
    try { // HSSFWorkbook 用于处理 .xls 格式

      // 创建一个工作表
      Sheet sheet = workbook.createSheet("Sheet1");

      // 创建一行并在第一行写入一些数据（示例）
      Row row = sheet.createRow(0);
      Cell cell = row.createCell(0);
      Cell cell2 = row.createCell(1);
      cell.setCellValue("Hello");
      cell2.setCellValue("Hello2");

      // 创建一个工作表
      Sheet sheet1 = workbook.createSheet("Sheet2");

      // 创建一行并在第一行写入一些数据（示例）
      Row row1 = sheet1.createRow(0);
      Cell cell1 = row1.createCell(0);
      Cell cell22 = row1.createCell(1);
      cell1.setCellValue("Work");
      cell22.setCellValue("Work1");

      // 将工作簿写入 ByteArrayOutputStream
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        workbook.write(outputStream);

        // 从 ByteArrayOutputStream 中获取 InputStream
        return new ByteArrayInputStream(outputStream.toByteArray());
      }
    } catch (Exception e) {

    }
    return null;
  }

  @Test
  public void getXlsSheetInfo() throws Exception {
    Map<String, Map<String, String>> sheetsInfo =
        XlsUtils.getSheetsInfo(createExcelAndGetInputStream(0), true);
    Assertions.assertTrue(sheetsInfo.containsKey("Sheet2"));
    Assertions.assertEquals("string", sheetsInfo.get("Sheet2").get("Work1"));
  }

  @Test
  public void getXlsxSheetInfo() throws Exception {
    Map<String, Map<String, String>> sheetsInfo =
        XlsxUtils.getSheetsInfo(createExcelAndGetInputStream(1), true);
    Assertions.assertTrue(sheetsInfo.containsKey("Sheet2"));
    Assertions.assertEquals("string", sheetsInfo.get("Sheet2").get("Work1"));
  }
}
