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

import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

  public InputStream createCSVInputStream(List<List<String>> data) {
    String csvData = convertToCSV(data);
    return new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
  }

  private String convertToCSV(List<List<String>> data) {
    StringBuilder csvData = new StringBuilder();

    for (List<String> row : data) {
      for (String column : row) {
        csvData.append(column).append(",");
      }
      csvData.deleteCharAt(csvData.length() - 1).append("\n");
    }

    return csvData.toString();
  }

  private Map<String, String> getCsvInfo(InputStream in, boolean escapeQuotes, boolean hasHeader)
      throws Exception {
    HashMap<String, String> csvMap = new LinkedHashMap<>();
    String[][] column = null;
    // fix csv file with utf-8 with bom chart[&#xFEFF]
    BOMInputStream bomIn = new BOMInputStream(in, false); // don't include the BOM
    BufferedReader reader = new BufferedReader(new InputStreamReader(bomIn, "utf-8"));

    String header = reader.readLine();
    if (StringUtils.isEmpty(header)) {
      throw new RuntimeException("内容为空");
    }
    String[] line = header.split(",", -1);
    int colNum = line.length;
    column = new String[2][colNum];
    if (hasHeader) {
      for (int i = 0; i < colNum; i++) {
        column[0][i] = line[i];
        if (escapeQuotes) {
          try {
            csvMap.put(column[0][i].substring(1, column[0][i].length() - 1), "string");
          } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException("处理标题引号异常");
          }
        } else {
          csvMap.put(column[0][i], "string");
        }
      }
    } else {
      for (int i = 0; i < colNum; i++) {
        csvMap.put("col_" + (i + 1), "string");
      }
    }
    csvMap.forEach((key, value) -> System.out.println(key + ": " + value));
    return csvMap;
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
        XlsxUtils.getAllSheetInfo(createExcelAndGetInputStream(1), null, true);
    Assertions.assertTrue(sheetsInfo.containsKey("Sheet2"));
    Assertions.assertEquals("string", sheetsInfo.get("Sheet2").get("Work1"));
  }

  @Test
  public void getCsvSheetInfo() throws Exception {
    List<List<String>> data = new ArrayList<>();
    data.add(Arrays.asList("Name", "Age", "City"));
    data.add(Arrays.asList("John Doe", "30", "New York"));
    data.add(Arrays.asList("Jane Smith", "25", "San Francisco"));

    // 有标题
    InputStream inputStream = createCSVInputStream(data);
    Map<String, String> csvMap = getCsvInfo(inputStream, false, true);
    Assertions.assertEquals("string", csvMap.get("Name"));

    // 无标题
    InputStream inputStream1 = createCSVInputStream(data);
    Map<String, String> csvMap1 = getCsvInfo(inputStream1, false, false);
    Assertions.assertEquals("string", csvMap1.get("col_1"));

    List<List<String>> data1 = new ArrayList<>();
    data1.add(Arrays.asList("'Name'", "'Age'", "'City'"));

    // 有标题有引号
    InputStream inputStream2 = createCSVInputStream(data1);
    Map<String, String> csvMap2 = getCsvInfo(inputStream2, true, true);
    Assertions.assertEquals("string", csvMap2.get("Name"));

    // 无标题
    InputStream inputStream3 = createCSVInputStream(data1);
    Map<String, String> csvMap3 = getCsvInfo(inputStream3, false, false);
    Assertions.assertEquals("string", csvMap3.get("col_1"));
  }
}
