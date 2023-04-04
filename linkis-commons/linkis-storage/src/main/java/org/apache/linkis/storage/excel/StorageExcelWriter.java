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

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageExcelWriter extends ExcelFsWriter {

  private static Logger logger = LoggerFactory.getLogger(StorageExcelWriter.class);

  private String charset;
  private String sheetName;
  private String dateFormat;
  private OutputStream outputStream;
  private boolean autoFormat;
  protected SXSSFWorkbook workBook;
  protected SXSSFSheet sheet;
  private DataFormat format;
  protected DataType[] types;
  protected int rowPoint;
  protected int columnCounter;
  protected Map<String, CellStyle> styles;
  private boolean isFlush;
  private ByteArrayOutputStream os;
  private ByteArrayInputStream is;

  public StorageExcelWriter(
      String charset,
      String sheetName,
      String dateFormat,
      OutputStream outputStream,
      boolean autoFormat) {
    this.charset = charset;
    this.sheetName = sheetName;
    this.dateFormat = dateFormat;
    this.outputStream = outputStream;
    this.autoFormat = autoFormat;
  }

  public void init() {
    workBook = new SXSSFWorkbook();
    sheet = workBook.createSheet(sheetName);
  }

  public CellStyle getDefaultHeadStyle() {
    Font headerFont = workBook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 14);
    headerFont.setColor(IndexedColors.RED.getIndex());
    CellStyle headerCellStyle = workBook.createCellStyle();
    headerCellStyle.setFont(headerFont);
    return headerCellStyle;
  }

  public Workbook getWorkBook() {
    // 自适应列宽
    sheet.trackAllColumnsForAutoSizing();
    for (int elem = 0; elem <= columnCounter; elem++) {
      sheet.autoSizeColumn(elem);
    }
    return workBook;
  }

  public CellStyle createCellStyle(DataType dataType) {
    CellStyle style = workBook.createCellStyle();
    format = workBook.createDataFormat();
    //        if ("default".equals(dataType.toString())) {
    style.setDataFormat(format.getFormat("@"));
    //        }
    if (autoFormat) {
      if (dataType instanceof DataType.StringType) {
        style.setDataFormat(format.getFormat("@"));
      } else if (dataType instanceof DataType.TinyIntType
          || dataType instanceof DataType.ShortIntType
          || dataType instanceof DataType.IntType) {
        style.setDataFormat(format.getFormat("#"));
      } else if (dataType instanceof DataType.LongType || dataType instanceof DataType.BigIntType) {
        style.setDataFormat(format.getFormat("#.##E+00"));
      } else if (dataType instanceof DataType.FloatType
          || dataType instanceof DataType.DoubleType) {
        style.setDataFormat(format.getFormat("#.0000000000"));
      } else if (dataType instanceof DataType.CharType
          || dataType instanceof DataType.VarcharType) {
        style.setDataFormat(format.getFormat("@"));
      } else if (dataType instanceof DataType.DateType
          || dataType instanceof DataType.TimestampType) {
        style.setDataFormat(format.getFormat("m/d/yy h:mm"));
      } else if (dataType instanceof DataType.DecimalType
          || dataType instanceof DataType.BigDecimalType) {
        style.setDataFormat(format.getFormat("#.000000000"));
      } else {
        style.setDataFormat(format.getFormat("@"));
      }
    }
    return style;
  }

  public CellStyle getCellStyle(DataType dataType) {
    CellStyle style = styles.get(dataType.getTypeName());
    if (style == null) {
      CellStyle newStyle = createCellStyle(dataType);
      styles.put(dataType.getTypeName(), newStyle);
      return newStyle;
    } else {
      return style;
    }
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    init();
    Row tableHead = sheet.createRow(0);
    Column[] columns = ((TableMetaData) metaData).getColumns();
    List<DataType> columnType = new ArrayList<>();
    for (int i = 0; i < columns.length; i++) {
      Cell headCell = tableHead.createCell(columnCounter);
      headCell.setCellValue(columns[i].getColumnName());
      headCell.setCellStyle(getDefaultHeadStyle());
      columnType.add(columns[i].getDataType());
      columnCounter++;
    }
    types = columnType.toArray(new DataType[0]);
    rowPoint++;
  }

  @Override
  public void addRecord(Record record) throws IOException {
    // TODO: 是否需要替换null值
    Row tableBody = sheet.createRow(rowPoint);
    int colunmPoint = 0;
    Object[] excelRecord = ((TableRecord) record).row;
    for (Object elem : excelRecord) {
      Cell cell = tableBody.createCell(colunmPoint);
      DataType dataType = types[colunmPoint];
      if (autoFormat) {
        setCellTypeValue(dataType, elem, cell);
      } else {
        cell.setCellValue(DataType.valueToString(elem));
      }
      cell.setCellStyle(getCellStyle(dataType));
      colunmPoint++;
    }
    rowPoint++;
  }

  private void setCellTypeValue(DataType dataType, Object elem, Cell cell) {
    if (null == elem) {
      return;
    }
    try {
      if (dataType instanceof DataType.StringType) {
        cell.setCellValue(DataType.valueToString(elem));
      } else if (dataType instanceof DataType.TinyIntType
          || dataType instanceof DataType.ShortIntType
          || dataType instanceof DataType.IntType) {
        cell.setCellValue(Integer.parseInt(elem.toString()));
      } else if (dataType instanceof DataType.LongType || dataType instanceof DataType.BigIntType) {
        cell.setCellValue(Long.parseLong(elem.toString()));
      } else if (dataType instanceof DataType.FloatType) {
        cell.setCellValue(Float.parseFloat(elem.toString()));
      } else if (dataType instanceof DataType.DoubleType
          || dataType instanceof DataType.DecimalType
          || dataType instanceof DataType.BigDecimalType) {
        cell.setCellValue(Double.parseDouble(DataType.valueToString(elem)));
      } else if (dataType instanceof DataType.CharType
          || dataType instanceof DataType.VarcharType) {
        cell.setCellValue(DataType.valueToString(elem));
      } else if (dataType instanceof DataType.DateType
          || dataType instanceof DataType.TimestampType) {
        cell.setCellValue(getDate(elem));
      } else {
        cell.setCellValue(DataType.valueToString(elem));
      }
    } catch (Exception e) {
      cell.setCellValue(DataType.valueToString(elem));
    }
  }

  private Date getDate(Object value) {
    if (value instanceof Date) {
      return (Date) value;
    } else {
      throw new NumberFormatException(
          "Value "
              + value
              + " with class : "
              + value.getClass().getName()
              + " is not a valid type of Date.");
    }
  }

  @Override
  public void flush() {
    try {
      getWorkBook().write(os);
    } catch (IOException e) {
      logger.warn("flush fail", e);
    }
    byte[] content = os.toByteArray();
    is = new ByteArrayInputStream(content);
    byte[] buffer = new byte[1024];
    int bytesRead = 0;
    while (isFlush) {
      try {
        bytesRead = is.read(buffer, 0, 1024);
        if (bytesRead == -1) {
          isFlush = false;
        } else {
          outputStream.write(buffer, 0, bytesRead);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void close() {
    if (isFlush) {
      flush();
    }
    IOUtils.closeQuietly(outputStream);
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(os);
    IOUtils.closeQuietly(workBook);
  }

  @Override
  public String getCharset() {
    return this.charset;
  }

  @Override
  public String getSheetName() {
    return this.sheetName;
  }

  @Override
  public String getDateFormat() {
    return this.dateFormat;
  }

  @Override
  public boolean isAutoFormat() {
    return this.autoFormat;
  }
}
