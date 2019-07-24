/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.filesystem.util;

import com.webank.wedatasphere.linkis.storage.domain.Column;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;

import java.util.Date;
import java.util.List;

/**
 * Created by johnnwang on 2018/11/5.
 */
@Deprecated
public class ExcelUtil {

    public static Workbook createWorkBook(String sheetName, Column[] columns, List<TableRecord> tableRecords){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("result");
        HSSFRow tableHead = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            HSSFCell cell = tableHead.createCell(i);
            cell.setCellValue(columns[i].columnName());
        }
        for (int i = 0; i < tableRecords.size(); i++) {
            HSSFRow tableBody = sheet.createRow(i+1);
            Object[] row = tableRecords.get(i).row();
            for (int j = 0; j < columns.length; j++) {
                HSSFCell cell = tableBody.createCell(j);
                HSSFCellStyle style = wb.createCellStyle();
                HSSFDataFormat format = wb.createDataFormat();
                switch (columns[j].dataType().toString()){
                    case "tinyint":
                    case "short":
                    case "int":
                    case "long":
                        style.setDataFormat(format.getFormat("0"));
                        cell.setCellValue(Long.parseLong(row[j].toString()));
                        cell.setCellStyle(style);
                        break;
                    case "float":
                    case "double":
                        style.setDataFormat(format.getFormat("0.00E+00"));
                        cell.setCellValue(Double.parseDouble(row[j].toString()));
                        cell.setCellStyle(style);
                        break;
                    case "date":
                    case "TimestampType":
                        style.setDataFormat(format.getFormat("YYYY年MM月dd日 HH:mm:ss"));
                        cell.setCellValue(new Date(Long.parseLong(row[j].toString())));
                        cell.setCellStyle(style);
                        break;
                    default:
                        style.setDataFormat(format.getFormat("@"));
                        cell.setCellValue(row[j].toString());
                        cell.setCellStyle(style);
                        break;
                }
            }
        }
        return wb;
    }


    public static Workbook create07WorkBook(String sheetName, Column[] columns, List<TableRecord> tableRecords){
        XSSFWorkbook  wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("result");
        XSSFRow tableHead = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            XSSFCell cell = tableHead.createCell(i);
            cell.setCellValue(columns[i].columnName());
        }
        for (int i = 0; i < tableRecords.size(); i++) {
            XSSFRow tableBody = sheet.createRow(i+1);
            Object[] row = tableRecords.get(i).row();
            for (int j = 0; j < columns.length; j++) {
                XSSFCell cell = tableBody.createCell(j);
                XSSFCellStyle style = wb.createCellStyle();
                XSSFDataFormat format = wb.createDataFormat();
                String a = columns[j].dataType().toString();
                switch (columns[j].dataType().toString()){
                    case "tinyint":
                    case "short":
                    case "int":
                    case "long":
                        style.setDataFormat(format.getFormat("#,#0"));
                        cell.setCellValue(Long.parseLong(row[j].toString()));
                        cell.setCellStyle(style);
                        break;
                    case "float":
                    case "double":
                        style.setDataFormat(format.getFormat("0.00E+00"));
                        cell.setCellValue(Double.parseDouble(row[j].toString()));
                        cell.setCellStyle(style);
                        break;
                    case "date":
                    case "TimestampType":
                        style.setDataFormat(format.getFormat("YYYY年MM月dd日 HH:mm:ss"));
                        cell.setCellValue(new Date(Long.parseLong(row[j].toString())));
                        cell.setCellStyle(style);
                        break;
                    default:
                        style.setDataFormat(format.getFormat("@"));
                        cell.setCellValue(row[j].toString());
                        cell.setCellStyle(style);
                        break;
                }
            }
        }
        return wb;
    }
}
