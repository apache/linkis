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

import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XlsUtils {
  private static final Logger LOG = LoggerFactory.getLogger(XlsUtils.class);

  public static List<List<String>> getBasicInfo(InputStream inputStream) {
    List<List<String>> res = new ArrayList<>();
    FirstRowDeal firstRowDeal = new FirstRowDeal();
    ExcelXlsReader xlsReader = new ExcelXlsReader();
    try {
      xlsReader.init(firstRowDeal, inputStream);
      xlsReader.process();
    } catch (ExcelAnalysisException e) {

      res.add(firstRowDeal.getSheetNames());
      res.add(firstRowDeal.getRow());

      LOG.info("Finshed to get xls Info");

    } catch (Exception e) {
      LOG.error("Failed to parse xls: ", e);
    } finally {
      xlsReader.close();
    }
    return res;
  }

  public static String excelToCsv(
      InputStream inputStream, FileSystem fs, Boolean hasHeader, List<String> sheetNames)
      throws Exception {
    String hdfsPath =
        "/tmp/" + StorageUtils.getJvmUser() + "/" + System.currentTimeMillis() + ".csv";
    LOG.info("The excel to csv with hdfs path:" + hdfsPath);
    ExcelXlsReader xlsReader = new ExcelXlsReader();
    RowToCsvDeal rowToCsvDeal = new RowToCsvDeal();
    OutputStream out = null;
    try {
      out = fs.create(new Path(hdfsPath));
      rowToCsvDeal.init(hasHeader, sheetNames, out);
      xlsReader.init(rowToCsvDeal, inputStream);
      xlsReader.process();
    } catch (IOException e) {
      LOG.error("Failed to excel to csv", e);
      throw e;
    } finally {
      if (out != null) {
        out.close();
      }
      xlsReader.close();
    }
    return hdfsPath;
  }

  public static Map<String, Map<String, String>> getSheetsInfo(
      InputStream inputStream, Boolean hasHeader) {
    // use xls file
    Workbook workbook = null;
    try {
      workbook = new HSSFWorkbook(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Map<String, Map<String, String>> res = new LinkedHashMap<>(workbook.getNumberOfSheets());
    // foreach Sheet
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);

      Map<String, String> sheetMap = new LinkedHashMap<>();

      // get first row as column name
      Row headerRow = sheet.getRow(0);

      // foreach column
      for (int j = 0; j < headerRow.getPhysicalNumberOfCells(); j++) {
        Cell cell = headerRow.getCell(j);
        if (hasHeader) {
          sheetMap.put(cell.getStringCellValue(), "string");
        } else {
          sheetMap.put("col_" + (j + 1), "string");
        }
      }
      res.put(sheet.getSheetName(), sheetMap);
    }
    return res;
  }
}
